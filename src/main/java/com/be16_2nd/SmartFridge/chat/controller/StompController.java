package com.be16_2nd.SmartFridge.chat.controller;

import com.be16_2nd.SmartFridge.chat.domain.ChatMessage;
import com.be16_2nd.SmartFridge.chat.dto.ChatMessageDto;
import com.be16_2nd.SmartFridge.chat.dto.ChatMessageEmailDto;
import com.be16_2nd.SmartFridge.chat.service.ChatRedisPubSubService;
import com.be16_2nd.SmartFridge.chat.service.ChatService;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import com.be16_2nd.SmartFridge.notification.domain.NotificationType;
import com.be16_2nd.SmartFridge.notification.service.NotificationPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
@Slf4j
public class StompController {
    private final ChatService chatService;
    private final ChatRedisPubSubService chatRedisPubSubService;
    private final NotificationPublisher notificationPublisher;
    private final FridgeMemberRepository fridgeMemberRepository;

    @MessageMapping("MANAGER/{roomId}")
    public void sendMessageToManager(@DestinationVariable Long roomId, ChatMessageDto chatMessageReqDto) throws JsonProcessingException {
        chatMessageReqDto.setChatRoomType("MANAGER");
        chatMessageReqDto.setRoomId(roomId);
        ChatMessage chatMessage = chatService.saveMessage(roomId, chatMessageReqDto);
        ChatMessageDto chatMessageDto = ChatMessageDto.fromEntityManagerChat(chatMessage);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(chatMessageDto);
        chatRedisPubSubService.publish("chat-channel", message);

        ChatMessageEmailDto result = chatService.saveMessageWithEmails(chatMessage);

        notificationPublisher.publish(
                chatMessage.getManagerChatRoom().getFridge().getId(),
//                result.senderEmail(),
                result.receiverEmailList().get(0),
                "새 메시지가 도착했습니다",
                NotificationType.ADMIN_CHAT.name()
        );
    }
    @MessageMapping("PURCHASE/{roomId}")
    public void sendMessageToGroup(@DestinationVariable Long roomId, ChatMessageDto chatMessageReqDto) throws JsonProcessingException {
        chatMessageReqDto.setChatRoomType("PURCHASE");
        chatMessageReqDto.setRoomId(roomId);
        ChatMessage chatMessage = chatService.saveMessage(roomId, chatMessageReqDto);
        ChatMessageDto chatMessageDto = ChatMessageDto.fromEntityPurchaseChat(chatMessage);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(chatMessageDto);
        chatRedisPubSubService.publish("chat-channel", message);
        ChatMessageEmailDto result = chatService.saveMessageWithEmails(chatMessage);

        for (String receiverEmail : result.receiverEmailList()) {
            notificationPublisher.publish(
                    chatMessage.getPurchaseChatRoom().getFridge().getId()
                    , receiverEmail
                    , "공동 구매 채팅방에 새로운 메세지가 도착했습니다."
                    , NotificationType.GROUP_CHAT.name()
            );
        }
    }
}
