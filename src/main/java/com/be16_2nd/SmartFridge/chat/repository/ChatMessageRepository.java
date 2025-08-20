package com.be16_2nd.SmartFridge.chat.repository;

import com.be16_2nd.SmartFridge.chat.domain.ChatMessage;
import com.be16_2nd.SmartFridge.chat.domain.ManagerChatRoom;
import com.be16_2nd.SmartFridge.chat.domain.PurchaseChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByManagerChatRoomOrderByCreatedTimeAsc(ManagerChatRoom managerChatRoom);
    List<ChatMessage> findByPurchaseChatRoomOrderByCreatedTimeAsc(PurchaseChatRoom purchaseChatRoom);
}
