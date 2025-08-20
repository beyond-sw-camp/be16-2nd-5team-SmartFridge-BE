package com.be16_2nd.SmartFridge.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyChatListResDto {
    private Long roomId;
    private String roomName;
    private Long unReadCount;
}
