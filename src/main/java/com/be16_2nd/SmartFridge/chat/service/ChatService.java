package com.be16_2nd.SmartFridge.chat.service;

import com.be16_2nd.SmartFridge.chat.domain.*;
import com.be16_2nd.SmartFridge.chat.dto.*;
import com.be16_2nd.SmartFridge.chat.repository.*;
import com.be16_2nd.SmartFridge.chat.service.Validator.ChatRoomParticipantValidator;
import com.be16_2nd.SmartFridge.common.service.FridgeAccessValidator;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.domain.Type;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import com.be16_2nd.SmartFridge.notification.domain.NotificationType;
import com.be16_2nd.SmartFridge.notification.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/*
* 채팅 서비스
*/

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ChatService {

    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final PurchasePostRepository purchasePostRepository;
    private final FridgeRepository fridgeRepository;
    private final FridgeMemberRepository fridgeMemberRepository;
    private final ChatImageService chatImageService;
    private final ChatRoomParticipantValidator chatRoomParticipantValidator;
    private final FridgeAccessValidator fridgeAccessValidator;
    private final ManagerChatRoomLifecycle managerChatRoomLifecycle;
    private final NotificationService notificationService;
    private final ChatPresence chatPresence;

    // 메시지 저장 (1대1 / 공동구매 공통)
    // senderEmail 은 인증된 세션 사용자여야 한다 (클라이언트가 보낸 값 사용 금지)
    public ChatMessage saveMessage(Long roomId, ChatRoomType chatRoomType, String senderEmail, ChatMessageDto chatMessageDto) {
        ChatRoom chatRoom = getRoom(roomId, chatRoomType);
        Member sender = chatRoomParticipantValidator.validateParticipant(senderEmail, roomId, chatRoomType).getMember();

        List<String> imageUrls = Optional.ofNullable(chatMessageDto.getImageUrls()).orElse(List.of());
        // 이 채팅방에 업로드된 이미지만 허용 (외부 URL 끼워 넣기 차단)
        chatImageService.validateImageUrls(chatRoomType, roomId, imageUrls);
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .messageType(imageUrls.isEmpty() ? MessageType.TEXT : MessageType.IMAGE)
                .contents(chatMessageDto.getMessage())
                .build();
        imageUrls.forEach(chatMessage::addImage);
        chatMessageRepository.save(chatMessage);
        chatRoom.updateLastMessageAt(chatMessage.getCreatedTime());

        // 보낸 사람과, 지금 채팅방을 보고 있는 참여자는 읽음 처리
        Set<String> online = chatPresence.onlineEmails(chatRoomType, roomId);
        for (ChatParticipant participant : chatRoom.getParticipants()) {
            String email = participant.getMember().getEmail();
            if (email.equals(sender.getEmail()) || online.contains(email)) {
                participant.readUpTo(chatMessage.getId());
            }
        }
        return chatMessage;
    }

    // 알림 받을 사람 = 보낸 사람을 제외한 참여자
    public ChatMessageEmailDto saveMessageWithEmails(ChatMessage chatMessage) {
        String senderEmail = chatMessage.getSender().getEmail();
        List<String> receiverEmails = chatMessage.getChatRoom().getParticipants().stream()
                .map(p -> p.getMember().getEmail())
                .filter(email -> !email.equals(senderEmail))
                .toList();
        return new ChatMessageEmailDto(chatMessage, senderEmail, receiverEmails);
    }

    // 내 채팅방 목록 조회
    public List<MyChatListResDto> getMyChatRooms(Long fridgeId){
        fridgeAccessValidator.validate(fridgeId);
        Member member = currentMember();
        Fridge fridge = fridgeRepository.findById(fridgeId).orElseThrow(()->new EntityNotFoundException("fridge not found"));

        List<MyChatListResDto> chatListResDtos = new ArrayList<>();

        // 관리자 채팅 방
        ChatRoom managerChatRoom = managerChatRoomLifecycle.findManagerChatRoom(fridge, member)
                .orElseThrow(()->new EntityNotFoundException("manager chat room not found"));
        chatListResDtos.add(MyChatListResDto.builder()
                .roomId(managerChatRoom.getId())
                .roomName("관리자와의 채팅")
                .unReadCount(managerChatRoomLifecycle.countUnread(managerChatRoom, member))
                .isManager(fridgeMemberRepository.findByFridgeAndMember(fridge, member).orElseThrow(()->new EntityNotFoundException("member not found")).getType().equals(Type.MANAGER))
                .build());

        // 공동 구매 채팅방
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByMemberAndChatRoomFridgeAndChatRoomType(member, fridge, ChatRoomType.PURCHASE);
        for(ChatParticipant chatParticipant : chatParticipants){
            ChatRoom chatRoom = chatParticipant.getChatRoom();
            PurchasePost post = chatRoom.getPurchasePost();
            chatListResDtos.add(MyChatListResDto.builder()
                    .roomId(chatRoom.getId())
                    .userName(post.getCreator().getName())
                    .currentParticipants(post.getCurrentParticipants())
                    .maxParticipants(post.getMaxParticipants())
                    .roomName(post.getTitle())
                    .unReadCount(chatMessageRepository.countUnread(chatRoom.getId(), chatParticipant.getLastReadMessageId(), member.getId()))
                    .isCreator(post.getCreator().getId().equals(member.getId()))
                    .JoinStatus(post.getJoinStatus().toString())
                    .build());
        }
        return chatListResDtos;
    }

    // 나의 채팅메시지 내역 조회
    public Page<ChatMessageDto> getChatHistory(ChatRoomType chatRoomType, Long roomId, Pageable pageable) {
        Member member = currentMember();
        ChatRoom chatRoom = getRoom(roomId, chatRoomType);

        // 관리자는 1대1 채팅 내역을 getManagerChatHistory 로 조회
        if (chatRoomType == ChatRoomType.MANAGER
                && fridgeMemberRepository.findByFridgeAndMember(chatRoom.getFridge(), member)
                    .orElseThrow(()->new EntityNotFoundException("fridge member not found")).getType().equals(Type.MANAGER)) {
            throw new IllegalArgumentException("잘못된 접근입니다.");
        }
        chatRoomParticipantValidator.validateParticipant(member.getEmail(), roomId, chatRoomType);

        return chatMessageRepository.findByChatRoom(chatRoom, pageable).map(ChatMessageDto::fromEntity);
    }

    // 관리자가 다른 유저의 채팅방 내역
    public Page<ChatMessageDto> getManagerChatHistory(Long roomId, Pageable pageable) {
        Member member = currentMember();
        ChatRoom chatRoom = getRoom(roomId, ChatRoomType.MANAGER);

        if (!fridgeMemberRepository.findByFridgeAndMember(chatRoom.getFridge(), member)
                .orElseThrow(() -> new EntityNotFoundException("fridge member not found")).getType().equals(Type.MANAGER)) {
            throw new IllegalArgumentException("잘못된 접근입니다.");
        }

        String receiverEmail = chatRoom.getParticipants().stream()
                .filter(p -> p.getRole() == ParticipantRole.MEMBER)
                .map(p -> p.getMember().getEmail())
                .findFirst()
                .orElse(null);

        return chatMessageRepository.findByChatRoom(chatRoom, pageable).map(chatMessage -> {
            ChatMessageDto dto = ChatMessageDto.fromEntity(chatMessage);
            dto.setReceiverEmail(receiverEmail);
            return dto;
        });
    }

    // 메시지 읽음 처리: 읽음 커서를 방의 마지막 메시지로 이동
    public void messageRead(Long roomId, ChatRoomType chatRoomType){
        Member member = currentMember();
        ChatRoom chatRoom = getRoom(roomId, chatRoomType);
        chatParticipantRepository.findByChatRoomAndMember(chatRoom, member)
                .ifPresent(p -> p.readUpTo(chatMessageRepository.findLastMessageId(roomId)));
    }

    // 공동 구매 채팅방 생성
    public Long createPurchaseChatRoom(ChatRoomCreateDto chatRoomCreateDto){
        Member member = currentMember();
        Fridge fridge = fridgeRepository.findById(chatRoomCreateDto.getFridgeId()).orElseThrow(() -> new EntityNotFoundException("fridge not found"));

        // 해당 냉장고 참여자인지 확인
        fridgeAccessValidator.validate(fridge.getId());

        ChatRoom chatRoom = ChatRoom.builder()
                .type(ChatRoomType.PURCHASE)
                .fridge(fridge)
                .build();
        chatRoom.attachPurchasePost(chatRoomCreateDto.toPurchasePost(member, chatRoom));
        // 개설자를 채팅 참여자로 추가
        chatRoom.addParticipant(member, ParticipantRole.MEMBER);
        chatRoomRepository.save(chatRoom);

        return chatRoom.getId();
    }

    // 공동 구매 채팅방 목록 조회
    public List<PurchaseChatRoomListResDto> getPurchaseChatRooms(Long fridgeID){
        Member member = currentMember();
        Fridge fridge = fridgeRepository.findById(fridgeID).orElseThrow(() -> new EntityNotFoundException("fridge not found"));

        // 해당 냉장고 참여자인지 확인
        fridgeAccessValidator.validate(fridge.getId());

        List<PurchasePost> posts = purchasePostRepository.findByChatRoomFridgeAndJoinStatusAndIsActiveOrderByCreatedTimeDesc(fridge, JoinStatus.OPEN, true);
        List<PurchaseChatRoomListResDto> result = new ArrayList<>();
        for(PurchasePost post : posts){
            result.add(PurchaseChatRoomListResDto.builder()
                    .roomId(post.getChatRoom().getId())
                    .roomName(post.getTitle())
                    .isCreator(post.getCreator().getId().equals(member.getId()))
                    .currentParticipants(post.getCurrentParticipants())
                    .maxParticipants(post.getMaxParticipants())
                    .joinStatus(post.getJoinStatus().toString())
                    .userName(post.getCreator().getName())
                    .build());
        }
        return result;
    }

    // 공동 구매 채팅방에 참여자 추가
    public void addParticipantToPurchaseChat(Long roomId){
        // 방 행을 잠가서 동시에 참여해도 정원 확인과 추가가 한 명씩 처리되게 한다
        // (잠금이 이 트랜잭션의 첫 조회여야 이후 조회가 앞선 참여 결과를 본다)
        ChatRoom chatRoom = chatRoomRepository.findByIdAndTypeForUpdate(roomId, ChatRoomType.PURCHASE)
                .orElseThrow(() -> new EntityNotFoundException("room cannot find"));
        Member member = currentMember();

        // 해당 냉장고 참여자인지 확인
        fridgeAccessValidator.validate(chatRoom.getFridge().getId());

        // 이미 참여자면 무시
        if (chatParticipantRepository.findByChatRoomAndMember(chatRoom, member).isPresent()) {
            return;
        }
        PurchasePost post = chatRoom.getPurchasePost();
        if (post.getJoinStatus() != JoinStatus.OPEN || !post.getIsActive()) {
            throw new IllegalArgumentException("모집이 끝난 공동구매입니다");
        }
        if (post.isFull()) {
            throw new IllegalArgumentException("정원이 초과되었습니다");
        }

        // 참여 전 메시지는 읽은 것으로 처리
        chatRoom.addParticipant(member, ParticipantRole.MEMBER)
                .readUpTo(chatMessageRepository.findLastMessageId(roomId));

        // 정원이 다 찬 경우 개설자에게만 알림
        if (post.isFull()) {
            notificationService.create(post.getCreator(), NotificationType.ROOM_FULL, post);
        }
    }

    // 공동 구매 채팅방 나가기
    public void leavePurchaseChatRoom(Long roomId){
        ChatRoom chatRoom = getRoom(roomId, ChatRoomType.PURCHASE);
        Member member = currentMember();

        // 해당 냉장고 참여자인지 확인
        fridgeAccessValidator.validate(chatRoom.getFridge().getId());

        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndMember(chatRoom, member)
                .orElseThrow(()->new EntityNotFoundException("채팅방 참여자가 아닙니다"));
        leaveRoom(chatRoom, chatParticipant);
    }

    // 냉장고에서 내보낼 시 해당 냉장고의 공동구매 채팅방에서 모두 나가기
    public void leaveForcePurchaseChatRoom(Long fridgeId, String memberEmail){
        Fridge fridge = fridgeRepository.findById(fridgeId).orElseThrow(()->new EntityNotFoundException("fridge not found"));

        // 권한 체크
        Member manager = currentMember();
        if(!fridgeMemberRepository.findByFridgeAndMember(fridge, manager).orElseThrow(()->new EntityNotFoundException("")).getType().equals(Type.MANAGER)){
            throw new AccessDeniedException("MANAGER만 가능합니다");
        }

        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(() -> new EntityNotFoundException("member not found"));
        leaveAllPurchaseChatRooms(fridge, member);
    }

    // 냉장고를 떠나는 회원을 그 냉장고의 공동구매 채팅방에서 모두 내보낸다 (방장이면 방 삭제)
    // 권한 확인은 호출하는 쪽(냉장고 나가기 / 강퇴)에서 한다
    public void leaveAllPurchaseChatRooms(Fridge fridge, Member member) {
        for (ChatParticipant chatParticipant : chatParticipantRepository.findByMemberAndChatRoomFridgeAndChatRoomType(member, fridge, ChatRoomType.PURCHASE)) {
            leaveRoom(chatParticipant.getChatRoom(), chatParticipant);
        }
    }

    // 방장이 나가면 채팅방 삭제(하드), 아니면 참여자만 제거
    private void leaveRoom(ChatRoom chatRoom, ChatParticipant chatParticipant) {
        if (chatRoom.getPurchasePost().getCreator().getId().equals(chatParticipant.getMember().getId())) {
            chatRoom.getFridge().getChatRooms().remove(chatRoom);
            chatRoomRepository.delete(chatRoom);
        } else {
            chatRoom.getParticipants().remove(chatParticipant);
        }
    }

    // 채팅 메시지 사진 저장
    public List<String> uploadImages(Long roomId, ChatRoomType chatRoomType, List<MultipartFile> files){
        Member member = currentMember();
        chatRoomParticipantValidator.validateParticipant(member.getEmail(), roomId, chatRoomType);
        return chatImageService.uploadImages(chatRoomType, roomId, files);
    }

    // 공동 구매 채팅방 정보 가져오기
    public PurchaseChatRoomResDto getRoomInfo(Long roomId){
        PurchasePost post = getPurchasePost(roomId);
        // 해당 냉장고 참여자인지 확인
        fridgeAccessValidator.validate(post.getChatRoom().getFridge().getId());
        return PurchaseChatRoomResDto.fromPurchasePost(post);
    }

    // 참여자 목록 가져오기
    public List<ParticipantsResDto> getParticipants(Long roomId){
        PurchasePost post = getPurchasePost(roomId);
        // 해당 냉장고 참여자인지 확인
        fridgeAccessValidator.validate(post.getChatRoom().getFridge().getId());
        Set<String> online = chatPresence.onlineEmails(ChatRoomType.PURCHASE, roomId);

        List<ParticipantsResDto> participantsResDtos = new ArrayList<>();
        for(ChatParticipant chatParticipant : post.getChatRoom().getParticipants()){
            Member member = chatParticipant.getMember();
            participantsResDtos.add(ParticipantsResDto.builder()
                    .memberName(member.getName())
                    .memberEmail(member.getEmail())
                    .isOnline(online.contains(member.getEmail()))
                    .isCreator(member.getId().equals(post.getCreator().getId()))
                    .profileImage(member.getProfileImage())
                    .build());
        }
        return participantsResDtos;
    }

    // 공동 구매 완료 버튼 누를 시 CLOSE상태로 전환
    public Long completePurchase(Long roomId){
        PurchasePost post = getPurchasePost(roomId);
        // 방장만 마감할 수 있다
        if (!post.getCreator().getId().equals(currentMember().getId())) {
            throw new AccessDeniedException("방장만 공동구매를 완료할 수 있습니다.");
        }
        post.updateJoinStatus(JoinStatus.CLOSE);
        return roomId;
    }

    private Member currentMember() {
        return memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new EntityNotFoundException("member not found"));
    }

    private ChatRoom getRoom(Long roomId, ChatRoomType type) {
        return chatRoomRepository.findByIdAndType(roomId, type)
                .orElseThrow(() -> new EntityNotFoundException("room cannot find"));
    }

    private PurchasePost getPurchasePost(Long roomId) {
        return purchasePostRepository.findByChatRoomId(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot find"));
    }
}
