package com.be16_2nd.SmartFridge.chat.repository;

import com.be16_2nd.SmartFridge.chat.domain.ChatParticipant;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoom;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.domain.ParticipantRole;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    Optional<ChatParticipant> findByChatRoomAndMember(ChatRoom chatRoom, Member member);
    Optional<ChatParticipant> findByChatRoomIdAndMemberEmail(Long chatRoomId, String email);
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

    // 냉장고 안에서 특정 타입 방에 참여 중인 목록
    List<ChatParticipant> findByMemberAndChatRoomFridgeAndChatRoomType(Member member, Fridge fridge, ChatRoomType type);

    // 1대1 채팅방: 방 주인(MEMBER 역할) 기준으로 조회
    Optional<ChatParticipant> findByMemberAndRoleAndChatRoomFridgeAndChatRoomType(Member member, ParticipantRole role, Fridge fridge, ChatRoomType type);

    // 1대1 채팅방: 냉장고의 관리자 참여 목록
    List<ChatParticipant> findByRoleAndChatRoomFridgeAndChatRoomType(ParticipantRole role, Fridge fridge, ChatRoomType type);
}
