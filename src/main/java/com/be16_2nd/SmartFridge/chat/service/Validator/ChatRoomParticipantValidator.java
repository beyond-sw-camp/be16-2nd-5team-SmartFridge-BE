package com.be16_2nd.SmartFridge.chat.service.Validator;

import com.be16_2nd.SmartFridge.chat.domain.ChatParticipant;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.repository.ChatParticipantRepository;
import com.be16_2nd.SmartFridge.chat.repository.ChatRoomRepository;
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

    // 채팅방 참여자 여부 확인 (1대1 방은 방 주인 + 냉장고 관리자가 참여자)
    @Transactional(readOnly = true)
    public ChatParticipant validateParticipant(String email, Long roomId, ChatRoomType chatRoomType) {
        chatRoomRepository.findByIdAndType(roomId, chatRoomType)
                .orElseThrow(() -> new EntityNotFoundException("해당 채팅방은 존재하지않습니다"));
        return chatParticipantRepository.findByChatRoomIdAndMemberEmail(roomId, email)
                .orElseThrow(() -> new AuthorizationServiceException("해당 채팅방에 권한이 없습니다."));
    }
}
