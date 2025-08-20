package com.be16_2nd.SmartFridge.chat.service.Validator;

import com.be16_2nd.SmartFridge.chat.domain.ManagerChatRoom;
import com.be16_2nd.SmartFridge.chat.domain.PurchaseChatRoom;
import com.be16_2nd.SmartFridge.chat.repository.ChatParticipantRepository;
import com.be16_2nd.SmartFridge.chat.repository.ManagerChatRoomRepository;
import com.be16_2nd.SmartFridge.chat.repository.PurchaseChatRoomRepository;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.domain.Type;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChatRoomParticipantValidator {

    private final ManagerChatRoomRepository managerChatRoomRepository;
    private final PurchaseChatRoomRepository purchaseChatRoomRepository;
    private final MemberRepository memberRepository;
    private final FridgeRepository fridgeRepository;
    private final FridgeMemberRepository fridgeMemberRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    // 냉장고 관리자 채팅방 참여자 확인
    public void validateManagerRoomParticipant(String email, Long roomId){
        ManagerChatRoom managerChatRoom = managerChatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("해당 채팅방은 존재하지않습니다"));
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다"));
        Fridge fridge = managerChatRoom.getFridge();

        // 해당 냉장고 관리자이거나 채팅방 참여자인지
        if(!fridgeMemberRepository.findByFridgeAndMember(fridge, member).orElseThrow(()->new EntityNotFoundException("냉장고 참여자가 아닙니다."))
                .getType().equals(Type.MANAGER) && !managerChatRoom.getMember().equals(member)){
            throw new AuthorizationServiceException("해당 채팅방에 권한이 없습니다.");
        }
    }

    // 공동 구매 채팅방 참여자 여부 확인
    public void validatePurchaseRoomParticipant(String email, Long roomId){
        PurchaseChatRoom purchaseChatRoom = purchaseChatRoomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("해당 채팅방은 존재하지않습니다"));
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다"));

        // 해당 채팅방 참여자인지
        if(!chatParticipantRepository.findByPurchaseChatRoomAndMember(purchaseChatRoom, member).isPresent()){
            throw new AuthorizationServiceException("해당 채팅방에 권한이 없습니다.");
        };
    }
}
