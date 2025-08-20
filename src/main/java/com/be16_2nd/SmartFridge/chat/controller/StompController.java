package com.be16_2nd.SmartFridge.chat.controller;

import com.be16_2nd.SmartFridge.chat.domain.ChatMessage;
import com.be16_2nd.SmartFridge.chat.dto.ChatMessageDto;
import com.be16_2nd.SmartFridge.chat.service.ChatRedisPubSubService;
import com.be16_2nd.SmartFridge.chat.service.ChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class StompController {
    private final ChatService chatService;
    private final ChatRedisPubSubService chatRedisPubSubService;

    @MessageMapping("MANAGER/{roomId}")
    public void sendMessageToManager(@DestinationVariable Long roomId, ChatMessageDto chatMessageReqDto) throws JsonProcessingException {
        chatMessageReqDto.setChatRoomType("MANAGER");
        chatMessageReqDto.setRoomId(roomId);
        ChatMessage chatMessage = chatService.saveMessage(roomId, chatMessageReqDto);
        ChatMessageDto chatMessageDto = ChatMessageDto.fromEntityManagerChat(chatMessage);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(chatMessageDto);
        chatRedisPubSubService.publish("chat-channel", message);
    }
    @MessageMapping("purchase/{roomId}")
    public void sendMessageToGroup(@DestinationVariable Long roomId, ChatMessageDto chatMessageReqDto) throws JsonProcessingException {
        chatMessageReqDto.setChatRoomType("PURCHASE");
        chatMessageReqDto.setRoomId(roomId);
        ChatMessage chatMessage = chatService.saveMessage(roomId, chatMessageReqDto);
        ChatMessageDto chatMessageDto = ChatMessageDto.fromEntityPurchaseChat(chatMessage);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(chatMessageDto);
        chatRedisPubSubService.publish("chat-channel", message);
    }
}
