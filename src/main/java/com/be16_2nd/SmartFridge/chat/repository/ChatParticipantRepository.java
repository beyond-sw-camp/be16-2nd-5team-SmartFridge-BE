package com.be16_2nd.SmartFridge.chat.repository;

import com.be16_2nd.SmartFridge.chat.domain.ChatParticipant;
import com.be16_2nd.SmartFridge.chat.domain.PurchaseChatRoom;
import com.be16_2nd.SmartFridge.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    Optional<ChatParticipant> findByPurchaseChatRoomAndMember(PurchaseChatRoom chatRoom, Member member);
    List<ChatParticipant> findByPurchaseChatRoom(PurchaseChatRoom chatRoom);
    List<ChatParticipant> findAllByMember(Member member);
}
