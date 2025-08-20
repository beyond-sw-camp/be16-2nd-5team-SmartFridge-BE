package com.be16_2nd.SmartFridge.chat.dto;

import com.be16_2nd.SmartFridge.chat.domain.ChatMessage;
import com.be16_2nd.SmartFridge.chat.domain.ChatMessageImage;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
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
    private List<String> imageUrls;

    private String timestamp;

    public static ChatMessageDto fromEntityManagerChat(ChatMessage chatMessage){
        return ChatMessageDto.builder()
                .roomId(chatMessage.getManagerChatRoom().getId())
                .chatRoomType(chatMessage.getChatRoomType().toString())
                .timestamp(chatMessage.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH시 mm분")))
                .imageUrls(chatMessage.getChatMessageImages().stream().map(ChatMessageImage::getImageUrl).toList())
                .message(chatMessage.getContents())
                .senderEmail(chatMessage.getSender().getEmail())
                .build();
    }

    public static ChatMessageDto fromEntityPurchaseChat(ChatMessage chatMessage){
        return ChatMessageDto.builder()
                .roomId(chatMessage.getPurchaseChatRoom().getId())
                .chatRoomType(chatMessage.getChatRoomType().toString())
                .timestamp(chatMessage.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH시 mm분")))
                .imageUrls(chatMessage.getChatMessageImages().stream().map(ChatMessageImage::getImageUrl).toList())
                .message(chatMessage.getContents())
                .senderEmail(chatMessage.getSender().getEmail())
                .build();
    }
}
