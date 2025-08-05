package com.be16_2nd.SmartFridge.fridge.dto;

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
public class FridgeMemberResDto {
    private Long fridgeId;
    private String memberEmail;
    private String memberName;
    private Type type;
    private LocalDateTime joinedTime;

    public static FridgeMemberResDto fromEntity(FridgeMember fridgeMember){
        return FridgeMemberResDto.builder()
                .fridgeId(fridgeMember.getFridge().getFridgeId())
                .memberEmail(fridgeMember.getMember().getEmail())
                .memberName(fridgeMember.getMember().getName())
                .type(fridgeMember.getType())
                .joinedTime(fridgeMember.getCreatedTime())
                .build();
    }
}
