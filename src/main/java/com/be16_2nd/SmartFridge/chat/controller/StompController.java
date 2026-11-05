package com.be16_2nd.SmartFridge.chat.controller;

import com.be16_2nd.SmartFridge.chat.domain.ChatMessage;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.dto.ChatMessageDto;
import com.be16_2nd.SmartFridge.chat.dto.ChatMessageEmailDto;
import com.be16_2nd.SmartFridge.chat.service.ChatRedisPubSubService;
import com.be16_2nd.SmartFridge.chat.service.ChatService;
import com.be16_2nd.SmartFridge.notification.domain.NotificationType;
import com.be16_2nd.SmartFridge.notification.service.NotificationPublisher;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
@Slf4j
public class StompController {
    private final ChatService chatService;
    private final ChatRedisPubSubService chatRedisPubSubService;
    private final NotificationPublisher notificationPublisher;

    // 발신자는 CONNECT 때 검증된 세션 사용자(principal)로 정한다. 요청 본문의 senderEmail은 무시
    @MessageMapping("MANAGER/{roomId}")
    public void sendMessageToManager(@DestinationVariable Long roomId, ChatMessageDto chatMessageReqDto, Principal principal) throws JacksonException {
        send(ChatRoomType.MANAGER, roomId, chatMessageReqDto, principal, NotificationType.MANAGER_CHAT);
    }

    @MessageMapping("PURCHASE/{roomId}")
    public void sendMessageToGroup(@DestinationVariable Long roomId, ChatMessageDto chatMessageReqDto, Principal principal) throws JacksonException {
        send(ChatRoomType.PURCHASE, roomId, chatMessageReqDto, principal, NotificationType.GROUP_CHAT);
    }

    private void send(ChatRoomType type, Long roomId, ChatMessageDto reqDto, Principal principal, NotificationType notificationType) throws JacksonException {
        ChatMessage chatMessage = chatService.saveMessage(roomId, type, principal.getName(), reqDto);
        String message = new ObjectMapper().writeValueAsString(ChatMessageDto.fromEntity(chatMessage));
        chatRedisPubSubService.publish("chat-channel", message);

        ChatMessageEmailDto result = chatService.saveMessageWithEmails(chatMessage);
        for (String receiverEmail : result.receiverEmailList()) {
            notificationPublisher.publish(
                    chatMessage.getChatRoom().getFridge().getId(),
                    receiverEmail,
                    notificationType.getDescription(),
                    notificationType.name()
            );
        }
    }
}
