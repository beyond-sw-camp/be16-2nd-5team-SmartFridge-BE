package com.be16_2nd.SmartFridge.fridge.dto;

import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FridgeCreateDto {
    private String fridgeName;
    private String description;

    public Fridge toEntity(){
        return Fridge.builder()
                .fridgeName(this.fridgeName)
                .description(this.description)
                .build();
    }
}
