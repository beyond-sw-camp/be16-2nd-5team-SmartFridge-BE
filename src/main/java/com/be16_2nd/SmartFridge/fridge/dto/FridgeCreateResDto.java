package com.be16_2nd.SmartFridge.fridge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FridgeCreateResDto {
    private Long fridgeId;
    private String inviteLink;
}
