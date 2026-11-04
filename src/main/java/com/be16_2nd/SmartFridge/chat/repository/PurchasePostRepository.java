package com.be16_2nd.SmartFridge.chat.repository;

import com.be16_2nd.SmartFridge.chat.domain.JoinStatus;
import com.be16_2nd.SmartFridge.chat.domain.PurchasePost;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchasePostRepository extends JpaRepository<PurchasePost, Long> {
    Optional<PurchasePost> findByChatRoomId(Long chatRoomId);
    List<PurchasePost> findByChatRoomFridgeAndJoinStatusAndIsActiveOrderByCreatedTimeDesc(Fridge fridge, JoinStatus joinStatus, Boolean isActive);
}
