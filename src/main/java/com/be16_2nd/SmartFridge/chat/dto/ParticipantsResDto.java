package com.be16_2nd.SmartFridge.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParticipantsResDto {
    private String memberName;
    private String memberEmail;
    private String profileImage;
    private Boolean isOnline;
    private Boolean isCreator;
}
