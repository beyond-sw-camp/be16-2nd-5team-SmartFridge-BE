package com.be16_2nd.SmartFridge.chat.dto;

import com.be16_2nd.SmartFridge.chat.domain.ChatMessage;
import com.be16_2nd.SmartFridge.chat.domain.ChatMessageImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatMessageDto {
    private Long roomId;
    private String chatRoomType;
    private String message;
    private String senderEmail;
    private String senderName;
    private List<String> imageUrls;
    private String receiverEmail;

    private String timestamp;

    public static ChatMessageDto fromEntityManagerChat(ChatMessage chatMessage){
        return ChatMessageDto.builder()
                .roomId(chatMessage.getManagerChatRoom().getId())
                .chatRoomType(chatMessage.getChatRoomType().toString())
                .timestamp(chatMessage.getCreatedTime().toString())
                .imageUrls(chatMessage.getChatMessageImages().stream().map(ChatMessageImage::getImageUrl).toList())
                .message(chatMessage.getContents())
                .senderEmail(chatMessage.getSender().getEmail())
                .senderName(chatMessage.getSender().getName())
                .build();
    }

    public static ChatMessageDto fromEntityPurchaseChat(ChatMessage chatMessage){
        return ChatMessageDto.builder()
                .roomId(chatMessage.getPurchaseChatRoom().getId())
                .chatRoomType(chatMessage.getChatRoomType().toString())
                .timestamp(chatMessage.getCreatedTime().toString())
                .imageUrls(chatMessage.getChatMessageImages().stream().map(ChatMessageImage::getImageUrl).toList())
                .message(chatMessage.getContents())
                .senderEmail(chatMessage.getSender().getEmail())
                .senderName(chatMessage.getSender().getName())
                .build();
    }
}
