package com.be16_2nd.SmartFridge.chat.repository;

import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.domain.ManagerChatRoom;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerChatRoomRepository extends JpaRepository<ManagerChatRoom, Long> {
    Optional<ManagerChatRoom> findByFridgeAndMember(Fridge fridge, Member member);
    void deleteByFridgeAndMember(Fridge fridge, Member member);
}
