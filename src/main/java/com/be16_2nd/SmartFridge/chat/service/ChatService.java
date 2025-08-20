package com.be16_2nd.SmartFridge.chat.service;

import com.be16_2nd.SmartFridge.chat.domain.*;
import com.be16_2nd.SmartFridge.chat.dto.ChatMessageDto;
import com.be16_2nd.SmartFridge.chat.dto.ChatRoomCreateDto;
import com.be16_2nd.SmartFridge.chat.dto.MyChatListResDto;
import com.be16_2nd.SmartFridge.chat.dto.PurchaseChatRoomListResDto;
import com.be16_2nd.SmartFridge.chat.repository.*;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.domain.Type;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

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

    // 내 채팅방 목록 조회
    public List<MyChatListResDto> getMyChatRooms(Long fridgeId){
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
                    .build();
            chatListResDtos.add(myChatListResDto);
        }

        // 공동 구매 채팅방
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByMember(member);
        for(ChatParticipant chatParticipant : chatParticipants){
            Long count = isReadRepository.countByMemberAndRoomIdAndChatRoomTypeAndIsReadFalse(member, chatParticipant.getPurchaseChatRoom().getId(), ChatRoomType.PURCHASE);
            MyChatListResDto myChatListResDto = MyChatListResDto.builder()
                    .roomId(chatParticipant.getPurchaseChatRoom().getId())
                    .roomName(chatParticipant.getPurchaseChatRoom().getTitle())
                    .unReadCount(count)
                    .build();
            chatListResDtos.add(myChatListResDto);
        }
        return chatListResDtos;
    }
    
    // 관리자 채팅 내역 조회
    public List<ChatMessageDto> getManagerChatHistory(Long roomId) {
        // 채팅방 찾기
        ManagerChatRoom managerChatRoom = managerChatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("room cannot find"));
        
        // 채팅방에 속해 있는 지 확인
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member not found"));
        if(!isManagerRoomParticipant(member.getEmail(), roomId)){
            throw new IllegalArgumentException("본인이 속하지 않은 채팅방입니다.");
        }

        // 메시지 조회
        List<ChatMessage>chatMessages = chatMessageRepository.findByManagerChatRoomOrderByCreatedTimeAsc(managerChatRoom);
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
                    .timestamp(chatMessage.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH시 mm분")))
                    .build();
            chatMessageDtos.add(chatMessageDto);
        }
        return chatMessageDtos;
    }

    // 공동구매 채팅 내역 조회
    public List<ChatMessageDto> getPurchaseChatHistory(Long roomId) {
        // 채팅방 찾기
        PurchaseChatRoom purchaseChatRoom = purchaseChatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("room cannot find"));
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member not found"));

        // 채팅방에 속해 있는 지 확인
        if(chatParticipantRepository.findByMemberAndPurchaseChatRoom(member, purchaseChatRoom).isEmpty()){
            throw new AuthorizationDeniedException("채팅방에 접근권한이 없습니다.");
        }

        // 메시지 조회
        List<ChatMessage>chatMessages = chatMessageRepository.findByPurchaseChatRoomOrderByCreatedTimeAsc(purchaseChatRoom);
        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();
        for(ChatMessage chatMessage : chatMessages){
            ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                    .chatRoomType(chatMessage.getChatRoomType().toString())
                    .message(chatMessage.getContents())
                    .senderEmail(chatMessage.getSender().getEmail())
                    .timestamp(chatMessage.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH시 mm분")))
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

    // 냉장고 관리자 채팅방 참여자 확인
    public boolean isManagerRoomParticipant(String email, Long roomId){
        ManagerChatRoom managerChatRoom = managerChatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room cannot find"));
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member not found"));
        Fridge fridge = managerChatRoom.getFridge();

        // 해당 냉장고 관리자이거나 채팅방 참여자인지
        if(fridgeMemberRepository.findByFridgeAndMember(fridge, member).orElseThrow(()->new EntityNotFoundException("냉장고 참여자가 아닙니다."))
                .getType().equals(Type.MANAGER) || managerChatRoom.getMember().equals(member)){
            return true;
        }
        return false;
    }

    // 공동 구매 채팅방 참여자 여부 확인
    public boolean isPurchaseRoomParticipant(String email, Long roomId){
        PurchaseChatRoom purchaseChatRoom = purchaseChatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room cannot find"));
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member not found"));
        Fridge fridge = purchaseChatRoom.getFridge();

        // 해당 채팅방 참여자인지
        return chatParticipantRepository.findByPurchaseChatRoomAndMember(purchaseChatRoom, member).isPresent();
    }

    // 공동 구매 채팅방 생성
    public Long createPurchaseChatRoom(ChatRoomCreateDto chatRoomCreateDto){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member not found"));
        Fridge fridge = fridgeRepository.findById(chatRoomCreateDto.getFridgeId()).orElseThrow(() -> new EntityNotFoundException("fridge not found"));

        // 해당 냉장고 참여자인지 확인
        if(!fridgeMemberRepository.findByFridgeAndMember(fridge, member).isPresent()){
            throw new EntityNotFoundException("해당 냉장고 참여자가 아닙니다.");
        }

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
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member not found"));
        Fridge fridge = fridgeRepository.findById(fridgeID).orElseThrow(() -> new EntityNotFoundException("fridge not found"));

        // 해당 냉장고 참여자인지 확인
        if(!fridgeMemberRepository.findByFridgeAndMember(fridge, member).isPresent()){
            throw new EntityNotFoundException("해당 냉장고 참여자가 아닙니다.");
        }

        List<PurchaseChatRoom> purchaseChatRooms = purchaseChatRoomRepository.findByFridgeAndJoinStatusAndIsActive(fridge, JoinStatus.OPEN, true);
        List<PurchaseChatRoomListResDto> purchaseChatRoomListResDtos = new ArrayList<>();
        for(PurchaseChatRoom purchaseChatRoom : purchaseChatRooms){
            PurchaseChatRoomListResDto purchaseChatRoomListResDto = PurchaseChatRoomListResDto.builder()
                    .roomId(purchaseChatRoom.getId())
                    .roomName(purchaseChatRoom.getTitle())
                    .build();
            purchaseChatRoomListResDtos.add(purchaseChatRoomListResDto);
        }
        return purchaseChatRoomListResDtos;
    }

    // 공동 구매 채팅방에 참여자 추가
    public void addParticipantToPurchaseChat(Long roomId){
        PurchaseChatRoom chatRoom = purchaseChatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("room cannot find"));
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("member not found"));

        // 이미 참여자인지 검증
        Optional<ChatParticipant> participant = chatParticipantRepository.findByPurchaseChatRoomAndMember(chatRoom, member);
        if(participant.isEmpty()){
            // 참여자가 다 찬 방인 지 확인
            if(chatRoom.getLimitedNum()<=0){
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
        chatRoom.updateLimitedNum(chatRoom.getLimitedNum()-1);
        chatRoom.getParticipants().add(chatParticipant);
    }

    // 공동 구매 채팅방 나가기
    public void leavePurchaseChatRoom(Long roomId){
        PurchaseChatRoom chatRoom = purchaseChatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("room cannot find"));
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("member not found"));

        // 참여자인 지 확인 후 제거
        ChatParticipant chatParticipant = chatParticipantRepository.findByPurchaseChatRoomAndMember(chatRoom, member).orElseThrow(()->new EntityNotFoundException("member not found"));
        chatParticipantRepository.delete(chatParticipant);
        chatRoom.updateLimitedNum(chatRoom.getLimitedNum()+1);
        
        // 방장이 나갈 시 채팅방 삭제(소프트)
        if(chatRoom.getCreator().equals(member)){
            chatRoom.updateIsActive(false);
        }
    }
    
    // 채팅 메시지 사진 저장
    public List<String> uploadImages(Long roomId, ChatRoomType chatRoomType, List<MultipartFile> files){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member not found"));
        // 1대1채팅일 경우
        if (chatRoomType.equals(ChatRoomType.MANAGER)){
            ManagerChatRoom chatRoom = managerChatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("room cannot find"));

//            채팅방참여자 검증
            if(!chatRoom.getMember().equals(member) && !fridgeMemberRepository.findByFridgeAndMember(chatRoom.getFridge(), member).orElseThrow().getType().equals(Type.MANAGER)){
                throw new IllegalArgumentException("해당 채팅방 참여자가 아닙니다.");
            }
            String pattern = "manager/chat/"+roomId+"/";
            return chatImageService.uploadImages(files, pattern);
        }
        // 공동구매채팅일 경우
        else{
            PurchaseChatRoom chatRoom = purchaseChatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("room cannot find"));
//            채팅방참여자 검증
            chatParticipantRepository.findByPurchaseChatRoomAndMember(chatRoom, member).orElseThrow(()->new IllegalArgumentException("해당 채팅방 참여자가 아닙니다."));
            String pattern = "manager/chat/"+roomId+"/";
            return chatImageService.uploadImages(files, pattern);
        }
    }
}
