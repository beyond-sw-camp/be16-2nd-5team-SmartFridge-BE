package com.be16_2nd.SmartFridge.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SseMessageDTO {
    private String type;
    private String senderEmail;
    private String receiverEmail;
    private Long fridgeId;
    private String contents;
}
