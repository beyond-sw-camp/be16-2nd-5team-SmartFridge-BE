package com.be16_2nd.SmartFridge.chat.service;

import com.be16_2nd.SmartFridge.chat.domain.*;
import com.be16_2nd.SmartFridge.chat.dto.*;
import com.be16_2nd.SmartFridge.chat.repository.*;
import com.be16_2nd.SmartFridge.chat.service.Validator.ChatRoomParticipantValidator;
import com.be16_2nd.SmartFridge.common.service.FridgeAccessValidator;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.domain.FridgeMember;
import com.be16_2nd.SmartFridge.fridge.domain.Type;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import com.be16_2nd.SmartFridge.notification.domain.NotificationType;
import com.be16_2nd.SmartFridge.notification.service.NotificationPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
* 채팅 서비스
*/

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ChatService {

    private final MemberRepository memberRepository;
    private final ManagerChatRoomRepository managerChatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final FridgeRepository fridgeRepository;
    private final IsReadRepository isReadRepository;
    private final FridgeMemberRepository fridgeMemberRepository;
    private final PurchaseChatRoomRepository purchaseChatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatImageService chatImageService;
    private final ChatRoomParticipantValidator chatRoomParticipantValidator;
    private final FridgeAccessValidator fridgeAccessValidator;
    private final NotificationPublisher notificationPublisher;

    public ChatMessage saveMessage(Long roomId, ChatMessageDto chatMessageDto) {

        Member sender = memberRepository.findByEmail(chatMessageDto.getSenderEmail()).orElseThrow(()->new EntityNotFoundException("room cannot find"));

        ChatMessage chatMessage = new ChatMessage();
        
        // 냉장고 관리자와의 1대1 채팅일 경우
        if(chatMessageDto.getChatRoomType().equals("MANAGER")){
            Fridge fridge = managerChatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("room cannot find")).getFridge();
            Member manager = fridgeMemberRepository.findByFridgeAndType(fridge, Type.MANAGER).orElseThrow(()->new EntityNotFoundException("member cannot find")).getMember();

            ManagerChatRoom chatRoom = managerChatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("room cannot find"));
            chatMessage = ChatMessage.builder()
                    .chatRoomType(ChatRoomType.MANAGER)
                    .managerChatRoom(chatRoom)
                    .sender(sender)
                    .contents(chatMessageDto.getMessage())
                    .isDeleted(false)
                    .build();
            chatMessageRepository.save(chatMessage);

            // 이미지 저장
            if(!chatMessageDto.getImageUrls().isEmpty()){
                for (String imageUrl : chatMessageDto.getImageUrls()){
                    chatMessage.getChatMessageImages().add(
                            ChatMessageImage.builder()
                                    .chatMessage(chatMessage)
                                    .imageUrl(imageUrl)
                                    .build()
                    );
                }
            }
            chatRoom.updateLastMessageAt(chatMessage.getCreatedTime());

            chatMessage.getIsReads().add(
                    IsRead.builder()
                        .roomId(roomId)
                        .member(sender)
                        .chatMessage(chatMessage)
                        .chatRoomType(ChatRoomType.MANAGER)
                        .isRead(true)
                        .build());

            Member unreadMember = sender.equals(manager) ? chatRoom.getMember() : manager;
            chatMessage.getIsReads().add(
                    IsRead.builder()
                        .roomId(roomId)
                        .member(unreadMember)
                        .chatRoomType(ChatRoomType.MANAGER)
                        .chatMessage(chatMessage)
                        .isRead(ManagerChatRoomLifecycle.ManagerRoomParticipants.get(roomId).contains(unreadMember.getEmail()))
                        .build());
        // 그룹 채팅일 경우
        }else if(chatMessageDto.getChatRoomType().equals("PURCHASE")){
           PurchaseChatRoom chatRoom = purchaseChatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("room cannot find"));

            chatMessage = ChatMessage.builder()
                    .chatRoomType(ChatRoomType.PURCHASE)
                    .purchaseChatRoom(chatRoom)
                    .sender(sender)
                    .contents(chatMessageDto.getMessage())
                    .isDeleted(false)
                    .build();
            chatMessageRepository.save(chatMessage);

            // 이미지 저장
            if(!chatMessageDto.getImageUrls().isEmpty()){
                for (String imageUrl : chatMessageDto.getImageUrls()){
                    chatMessage.getChatMessageImages().add(
                            ChatMessageImage.builder()
                                    .chatMessage(chatMessage)
                                    .imageUrl(imageUrl)
                                    .build()
                    );
                }
            }
            chatRoom.updateLastMessageAt(chatMessage.getCreatedTime());

            // 사용자별로 읽음여부 저장
            List<ChatParticipant> chatParticipants = chatParticipantRepository.findByPurchaseChatRoom(chatRoom);
            for(ChatParticipant chatParticipant : chatParticipants){
                chatMessage.getIsReads().add(
                        IsRead.builder()
                                .roomId(roomId)
                                .member(chatParticipant.getMember())
                                .chatRoomType(ChatRoomType.PURCHASE)
                                .chatMessage(chatMessage)
                                .isRead(ManagerChatRoomLifecycle.PurchaseRoomParticipants.get(roomId).contains(chatParticipant.getMember().getEmail()))
                                .build());
            }
        }
        return chatMessage;
    }

    public ChatMessageEmailDto saveMessageWithEmails(ChatMessage chatMessage) {
        String senderEmail = chatMessage.getSender().getEmail();
        List<String> receiverEmails;

        if (chatMessage.getChatRoomType().equals(ChatRoomType.MANAGER)) {
            // 1:1 관리자 채팅
            Fridge fridge = chatMessage.getManagerChatRoom().getFridge();
            FridgeMember managerFridgeMember = fridgeMemberRepository
                    .findByFridgeAndType(fridge, Type.MANAGER)
                    .orElseThrow(() -> new RuntimeException("관리자가 존재하지 않습니다."));

            String receiverEmail = managerFridgeMember.getMember().getEmail();

            // 발신자가 냉장고 관리자면 수신자는 냉장고 참여자
            if (senderEmail.equals(receiverEmail)) {
                receiverEmail = chatMessage.getManagerChatRoom().getMember().getEmail();
            }

            receiverEmails = List.of(receiverEmail);

        } else if (chatMessage.getChatRoomType().equals(ChatRoomType.PURCHASE)) {
            // 공동구매 채팅
            receiverEmails = chatMessage.getPurchaseChatRoom().getParticipants().stream()
                    .map(ChatParticipant::getMember)
                    .map(Member::getEmail)
                    .filter(email -> !email.equals(senderEmail))
                    .toList();
        } else {
            throw new IllegalArgumentException("존재하지 않는 채팅 타입입니다.");
        }

        return new ChatMessageEmailDto(chatMessage, senderEmail, receiverEmails);
    }

    // 내 채팅방 목록 조회
    public List<MyChatListResDto> getMyChatRooms(Long fridgeId){
        fridgeAccessValidator.validate(fridgeId);
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member not found"));
        Fridge fridge = fridgeRepository.findById(fridgeId).orElseThrow(()->new EntityNotFoundException("fridge not found"));

        List<MyChatListResDto> chatListResDtos = new ArrayList<>();

        // 관리자 채팅 방
        ManagerChatRoom managerChatRoom = managerChatRoomRepository.findByFridgeAndMember(fridge, member).orElseThrow(()->new EntityNotFoundException("manager chat room not found"));
        if(managerChatRoom != null){
            Long count = isReadRepository.countByMemberAndRoomIdAndChatRoomTypeAndIsReadFalse(member, managerChatRoom.getId(), ChatRoomType.MANAGER);
            MyChatListResDto myChatListResDto = MyChatListResDto.builder()
                    .roomId(managerChatRoom.getId())
                    .roomName("관리자와의 채팅")
                    .unReadCount(count)
                    .isManager(fridgeMemberRepository.findByFridgeAndMember(fridge, member).orElseThrow(()->new EntityNotFoundException("member not found")).getType().equals(Type.MANAGER))
                    .build();
            chatListResDtos.add(myChatListResDto);
        }

        // 공동 구매 채팅방
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByMember(member);
        for(ChatParticipant chatParticipant : chatParticipants){
            if(chatParticipant.getPurchaseChatRoom().getFridge().getId().equals(fridge.getId())){
                Long count = isReadRepository.countByMemberAndRoomIdAndChatRoomTypeAndIsReadFalse(member, chatParticipant.getPurchaseChatRoom().getId(), ChatRoomType.PURCHASE);
                MyChatListResDto myChatListResDto = MyChatListResDto.builder()
                        .roomId(chatParticipant.getPurchaseChatRoom().getId())
                        .userName(member.getName())
                        .currentParticipants(chatParticipant.getPurchaseChatRoom().getCurrentParticipants())
                        .maxParticipants(chatParticipant.getPurchaseChatRoom().getMaxParticipants())
                        .roomName(chatParticipant.getPurchaseChatRoom().getTitle())
                        .unReadCount(count)
                        .isCreator(chatParticipant.getPurchaseChatRoom().getCreator().getId().equals(member.getId()))
                        .build();
                chatListResDtos.add(myChatListResDto);
            }
        }
        return chatListResDtos;
    }

    // 나의 채팅메시지 내역 조회
    public List<ChatMessageDto> getChatHistory(ChatRoomType chatRoomType, Long roomId) {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("존재하지 않은 회원입니다"));
        List<ChatMessage> chatMessages = new ArrayList<>();

        // 관리자 채팅 내역 조회
        if (chatRoomType.equals(ChatRoomType.MANAGER)){
            // 채팅방 찾기
            ManagerChatRoom managerChatRoom = managerChatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("room cannot find"));

            // 냉장고 관리자가 본인의 채팅방 접근 시 예외 발생
            if(fridgeMemberRepository.findByFridgeAndMember(managerChatRoom.getFridge(), member).orElseThrow(()->new EntityNotFoundException("fridge member not found")).getType().equals(Type.MANAGER)){
                throw new IllegalArgumentException("잘못된 접근입니다.");
            }

            // 채팅방에 속한 회원인지 검증
            chatRoomParticipantValidator.validateManagerRoomParticipant(member.getEmail(), roomId);

            // 메시지 조회
            chatMessages = chatMessageRepository.findByManagerChatRoomOrderByCreatedTimeAsc(managerChatRoom);
        }
        // 공동 구매 채팅 내역 조회
        else{
            // 채팅방 찾기
            PurchaseChatRoom purchaseChatRoom = purchaseChatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("room cannot find"));

            // 채팅방에 속한 회원인지 검증
            chatRoomParticipantValidator.validatePurchaseRoomParticipant((member.getEmail()), roomId);

            // 메시지 조회
            chatMessages = chatMessageRepository.findByPurchaseChatRoomOrderByCreatedTimeAsc(purchaseChatRoom);
        }

        // 메시지를 응답 메시지DTO에 담아 return
        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();
        for(ChatMessage chatMessage : chatMessages){
            List<String> imageUrls = new ArrayList<>();
            for (ChatMessageImage chatMessageImage : chatMessage.getChatMessageImages()){
                imageUrls.add(chatMessageImage.getImageUrl());
            }
            ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                    .chatRoomType(chatMessage.getChatRoomType().toString())
                    .message(chatMessage.getContents())
                    .imageUrls(imageUrls)
                    .senderEmail(chatMessage.getSender().getEmail())
                    .senderName(chatMessage.getSender().getName())
                    .timestamp(chatMessage.getCreatedTime().toString())
                    .build();
            chatMessageDtos.add(chatMessageDto);
        }
        return chatMessageDtos;
    }

    // 관리자가 다른 유저의 채팅방 내역
    public List<ChatMessageDto> getManagerChatHistory(Long roomId) {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("존재하지 않은 회원입니다"));
        List<ChatMessage> chatMessages = new ArrayList<>();
        ManagerChatRoom managerChatRoom = managerChatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("room cannot find"));

        // 해당 냉장고의 매니저가 아니면 예외 처리
        if(!fridgeMemberRepository.findByFridgeAndMember(managerChatRoom.getFridge(), member).orElseThrow(()->new EntityNotFoundException("fridge member not found")).getType().equals(Type.MANAGER)){
            throw new IllegalArgumentException("잘못된 접근입니다.");
        }

        // 메시지 조회
        chatMessages = chatMessageRepository.findByManagerChatRoomOrderByCreatedTimeAsc(managerChatRoom);
        // 메시지를 응답 메시지DTO에 담아 return
        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();
        for(ChatMessage chatMessage : chatMessages){
            List<String> imageUrls = new ArrayList<>();
            for (ChatMessageImage chatMessageImage : chatMessage.getChatMessageImages()){
                imageUrls.add(chatMessageImage.getImageUrl());
            }
            ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                    .chatRoomType(chatMessage.getChatRoomType().toString())
                    .message(chatMessage.getContents())
                    .imageUrls(imageUrls)
                    .senderEmail(chatMessage.getSender().getEmail())
                    .senderName(chatMessage.getSender().getName())
                    .timestamp(chatMessage.getCreatedTime().toString())
                    .build();
            chatMessageDtos.add(chatMessageDto);
        }
        return chatMessageDtos;
    }

    
    // 메시지 읽음 처리
    public void messageRead(Long roomId, ChatRoomType chatRoomType){
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member not found"));

        List<IsRead> isReads = new ArrayList<>();
        if(chatRoomType.equals(ChatRoomType.MANAGER)){
            isReads = isReadRepository.findAllByRoomIdAndMemberAndChatRoomType(roomId, member, ChatRoomType.MANAGER);
        }
        else if(chatRoomType.equals(ChatRoomType.PURCHASE)){
            isReads = isReadRepository.findAllByRoomIdAndMemberAndChatRoomType(roomId, member, ChatRoomType.PURCHASE);
        }
        for(IsRead isRead : isReads){
            isRead.updateIsRead(true);
        }
    }

    // 공동 구매 채팅방 생성
    public Long createPurchaseChatRoom(ChatRoomCreateDto chatRoomCreateDto){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member not found"));
        Fridge fridge = fridgeRepository.findById(chatRoomCreateDto.getFridgeId()).orElseThrow(() -> new EntityNotFoundException("fridge not found"));

        // 해당 냉장고 참여자인지 확인
        fridgeAccessValidator.validate(fridge.getId());

        PurchaseChatRoom purchaseChatRoom = chatRoomCreateDto.toPurchaseChatRoom(member, fridge);
        purchaseChatRoomRepository.save(purchaseChatRoom);
        
        // 개설자를 채팅 참여자로 추가
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .purchaseChatRoom(purchaseChatRoom)
                .member(member)
                .build();
        purchaseChatRoom.getParticipants().add(chatParticipant);

        return purchaseChatRoom.getId();
    }

    // 공동 구매 채팅방 목록 조회
    public List<PurchaseChatRoomListResDto> getPurchaseChatRooms(Long fridgeID){
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member not found"));
        Fridge fridge = fridgeRepository.findById(fridgeID).orElseThrow(() -> new EntityNotFoundException("fridge not found"));

        // 해당 냉장고 참여자인지 확인
        fridgeAccessValidator.validate(fridge.getId());

        List<PurchaseChatRoom> purchaseChatRooms = purchaseChatRoomRepository.findByFridgeAndJoinStatusAndIsActiveOrderByCreatedTimeDesc(fridge, JoinStatus.OPEN, true);
        List<PurchaseChatRoomListResDto> purchaseChatRoomListResDtos = new ArrayList<>();
        for(PurchaseChatRoom purchaseChatRoom : purchaseChatRooms){
            PurchaseChatRoomListResDto purchaseChatRoomListResDto = PurchaseChatRoomListResDto.builder()
                    .roomId(purchaseChatRoom.getId())
                    .roomName(purchaseChatRoom.getTitle())
                    .isCreator(purchaseChatRoom.getCreator().getId().equals(member.getId()))
                    .currentParticipants(purchaseChatRoom.getCurrentParticipants())
                    .maxParticipants(purchaseChatRoom.getMaxParticipants())
                    .build();
            purchaseChatRoomListResDtos.add(purchaseChatRoomListResDto);
        }
        return purchaseChatRoomListResDtos;
    }

    // 공동 구매 채팅방에 참여자 추가
    public void addParticipantToPurchaseChat(Long roomId){
        PurchaseChatRoom chatRoom = purchaseChatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("room cannot find"));
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("member not found"));

        // 해당 냉장고 참여자인지 확인
        fridgeAccessValidator.validate(chatRoom.getFridge().getId());

        // 이미 참여자인지 검증
        Optional<ChatParticipant> participant = chatParticipantRepository.findByPurchaseChatRoomAndMember(chatRoom, member);
        if(participant.isEmpty()){
            // 참여자가 다 찬 방인 지 확인
            if(chatRoom.getCurrentParticipants().equals(chatRoom.getMaxParticipants())){
                throw new IllegalArgumentException("정원이 초과되었습니다");
            }
            addParticipantToRoom(chatRoom, member);
        }
    }

    // 공동구매채팅방참여
    public void addParticipantToRoom(PurchaseChatRoom chatRoom, Member member){
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .purchaseChatRoom(chatRoom)
                .member(member)
                .build();
        chatRoom.updateCurrentParticipants(chatRoom.getCurrentParticipants()+1);
        chatRoom.getParticipants().add(chatParticipant);

        // 남은 인원 수 0인 경우 확인
        if(chatRoom.getCurrentParticipants().equals(chatRoom.getMaxParticipants())){

            // 공동 구매 채팅방 개설자에게만 발송
            Member receiver = chatRoom.getCreator();

            notificationPublisher.publish(
                    chatRoom.getFridge().getId()
                    , receiver.getEmail()
                    , "공동 구매 채팅방이 가득 찼습니다"
                    , "ROOM_FULL"
            );
        }
    }

    // 공동 구매 채팅방 나가기
    public void leavePurchaseChatRoom(Long roomId){
        PurchaseChatRoom chatRoom = purchaseChatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room cannot find"));
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member not found"));

        // 해당 냉장고 참여자인지 확인
        fridgeAccessValidator.validate(chatRoom.getFridge().getId());

        // 참여자인 지 확인 후 제거
        ChatParticipant chatParticipant = chatParticipantRepository.findByPurchaseChatRoomAndMember(chatRoom, member).orElseThrow(()->new EntityNotFoundException("채팅방 참여자가 아닙니다"));
        chatParticipantRepository.delete(chatParticipant);
        chatRoom.updateCurrentParticipants(chatRoom.getCurrentParticipants()-1);

        // 방장이 나갈 시 채팅방 삭제(소프트)
        if(chatRoom.getCreator().equals(member)){
            chatRoom.updateIsActive(false);
        }
    }

    // 냉장고 나갈 시
    public void leaveForcePurchaseChatRoom(Long fridgeId, String memberEmail){
        Fridge fridge = fridgeRepository.findById(fridgeId).orElseThrow(()->new EntityNotFoundException("fridge not found"));

        // 권한 체크
        Member manager = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("member not found"));
        if(!fridgeMemberRepository.findByFridgeAndMember(fridge, manager).orElseThrow(()->new EntityNotFoundException("")).getType().equals(Type.MANAGER)){
            throw new AccessDeniedException("MANAGER만 가능합니다");
        }
        
        List<PurchaseChatRoom> chatRooms = purchaseChatRoomRepository.findByFridge(fridge);
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(() -> new EntityNotFoundException("member not found"));
        

        for(PurchaseChatRoom purchaseChatRoom : chatRooms){
            if(chatParticipantRepository.findByPurchaseChatRoomAndMember(purchaseChatRoom, member).isPresent()){
                ChatParticipant chatParticipant = chatParticipantRepository.findByPurchaseChatRoomAndMember(purchaseChatRoom, member).get();
                chatParticipantRepository.delete(chatParticipant);
                purchaseChatRoom.updateCurrentParticipants(purchaseChatRoom.getCurrentParticipants()-1);
            }

            // 방장이 나갈 시 채팅방 삭제(소프트)
            if(purchaseChatRoom.getCreator().equals(member)){
                purchaseChatRoom.updateIsActive(false);
            }
        }
    }


    
    // 채팅 메시지 사진 저장
    public List<String> uploadImages(Long roomId, ChatRoomType chatRoomType, List<MultipartFile> files){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member not found"));
        // 1대1채팅일 경우
        if (chatRoomType.equals(ChatRoomType.MANAGER)){
//            채팅방참여자 검증
            chatRoomParticipantValidator.validateManagerRoomParticipant(member.getEmail(), roomId);
            String pattern = "manager/chat/"+roomId+"/";
            return chatImageService.uploadImages(files, pattern);
        }
        // 공동구매채팅일 경우
        else{
//            채팅방참여자 검증
            chatRoomParticipantValidator.validatePurchaseRoomParticipant(member.getEmail(), roomId);
            String pattern = "manager/chat/"+roomId+"/";
            return chatImageService.uploadImages(files, pattern);
        }
    }

    // 공동 구매 채팅방 정보 가져오기
    public PurchaseChatRoomResDto getRoomInfo(Long roomId){
        PurchaseChatRoom purchaseChatRoom = purchaseChatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("room cannot find"));
        return PurchaseChatRoomResDto.fromPurchaseChatRoom(purchaseChatRoom);
    }
}