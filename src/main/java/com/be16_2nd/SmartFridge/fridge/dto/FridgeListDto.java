package com.be16_2nd.SmartFridge.fridge.dto;

import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.domain.FridgeMember;
import com.be16_2nd.SmartFridge.fridge.domain.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FridgeListDto {
    private Long fridgeId;
    private String fridgeName;
    private String description;
    private Type type;
    private String inviteLink;
    private LocalDateTime createdTime;

    public static FridgeListDto from(FridgeMember fridgeMember) {
        Fridge fridge = fridgeMember.getFridge();
        return FridgeListDto.builder()
                .fridgeId(fridge.getId())
                .fridgeName(fridge.getFridgeName())
                .description(fridge.getDescription())
                .type(fridgeMember.getType())
                .inviteLink(fridge.getInviteCode())
                .createdTime(fridge.getCreatedTime())
                .build();
    }

    public static FridgeListDto fromEntity(Fridge fridge,Type type) {
        return FridgeListDto.builder()
                .fridgeId(fridge.getId())
                .fridgeName(fridge.getFridgeName())
                .description(fridge.getDescription())
                .type(type)
                .createdTime(fridge.getCreatedTime())
                .build();
    }
}