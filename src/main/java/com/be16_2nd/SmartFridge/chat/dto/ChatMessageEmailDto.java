package com.be16_2nd.SmartFridge.chat.dto;

import com.be16_2nd.SmartFridge.chat.domain.ChatMessage;

import java.util.List;

public record ChatMessageEmailDto(ChatMessage chatMessage
        , String senderEmail
        , List<String> receiverEmailList) {

}
