package com.be16_2nd.SmartFridge.chat.repository;

import com.be16_2nd.SmartFridge.chat.domain.JoinStatus;
import com.be16_2nd.SmartFridge.chat.domain.PurchaseChatRoom;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseChatRoomRepository extends JpaRepository<PurchaseChatRoom, Long> {
    List<PurchaseChatRoom> findByFridgeAndJoinStatusAndIsActiveOrderByCreatedTimeDesc(Fridge fridge, JoinStatus joinStatus, Boolean isActive);
    List<PurchaseChatRoom> findByFridge(Fridge fridge);
}
