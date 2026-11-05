package com.be16_2nd.SmartFridge.chat.repository;

import com.be16_2nd.SmartFridge.chat.domain.ChatParticipant;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoom;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.domain.ParticipantRole;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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

    // 내 공동구매 채팅방 목록: 방·게시글·방장을 함께 조회 (방마다 추가 조회 방지)
    @Query("select cp from ChatParticipant cp join fetch cp.chatRoom r join fetch r.purchasePost pp join fetch pp.creator " +
            "where cp.member = :member and r.fridge = :fridge " +
            "and r.type = com.be16_2nd.SmartFridge.chat.domain.ChatRoomType.PURCHASE")
    List<ChatParticipant> findMyPurchaseParticipations(@Param("member") Member member, @Param("fridge") Fridge fridge);

    // 냉장고의 1대1 채팅방 주인(MEMBER 역할) 회원 id -> 방 id
    // ChatRoom 엔티티를 읽으면 purchasePost(@OneToOne mappedBy, 지연 로딩 불가) 확인 쿼리가 방마다 나가므로 id 만 조회한다
    @Query("select cp.member.id as memberId, cp.chatRoom.id as roomId from ChatParticipant cp " +
            "where cp.chatRoom.fridge = :fridge and cp.chatRoom.type = com.be16_2nd.SmartFridge.chat.domain.ChatRoomType.MANAGER " +
            "and cp.role = com.be16_2nd.SmartFridge.chat.domain.ParticipantRole.MEMBER")
    List<OwnerRoom> findManagerRoomOwners(@Param("fridge") Fridge fridge);

    interface OwnerRoom {
        java.util.UUID getMemberId();
        Long getRoomId();
    }

    // 방별 참여자 수
    @Query("select cp.chatRoom.id as roomId, count(cp) as count from ChatParticipant cp " +
            "where cp.chatRoom.id in :roomIds group by cp.chatRoom.id")
    List<RoomCount> countByRoomIds(@Param("roomIds") Collection<Long> roomIds);
}
