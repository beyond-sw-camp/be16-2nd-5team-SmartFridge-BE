package com.be16_2nd.SmartFridge.chat.service.Validator;

import com.be16_2nd.SmartFridge.chat.domain.ChatParticipant;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoom;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.repository.ChatParticipantRepository;
import com.be16_2nd.SmartFridge.chat.repository.ChatRoomRepository;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ChatRoomParticipantValidator {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final FridgeMemberRepository fridgeMemberRepository;

    // 채팅방 참여자 여부 확인 (1대1 방은 방 주인 + 냉장고 관리자가 참여자)
    // 참여자로 남아 있어도 냉장고를 떠난 회원이면 거부한다
    @Transactional(readOnly = true)
    public ChatParticipant validateParticipant(String email, Long roomId, ChatRoomType chatRoomType) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndType(roomId, chatRoomType)
                .orElseThrow(() -> new EntityNotFoundException("해당 채팅방은 존재하지않습니다"));
        ChatParticipant participant = chatParticipantRepository.findByChatRoomIdAndMemberEmail(roomId, email)
                .orElseThrow(() -> new AuthorizationServiceException("해당 채팅방에 권한이 없습니다."));
        if (!fridgeMemberRepository.existsByFridgeAndMember(chatRoom.getFridge(), participant.getMember())) {
            throw new AuthorizationServiceException("해당 채팅방에 권한이 없습니다.");
        }
        return participant;
    }
}
