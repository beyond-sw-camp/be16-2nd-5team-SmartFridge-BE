package com.be16_2nd.SmartFridge.chat.repository;

import com.be16_2nd.SmartFridge.chat.domain.JoinStatus;
import com.be16_2nd.SmartFridge.chat.domain.PurchaseChatRoom;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseChatRoomRepository extends JpaRepository<PurchaseChatRoom, Long> {
    List<PurchaseChatRoom> findByFridgeAndJoinStatusAndIsActive(Fridge fridge, JoinStatus joinStatus, Boolean isActive);
}
