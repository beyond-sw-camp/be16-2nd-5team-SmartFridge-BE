package com.be16_2nd.SmartFridge.food.dto;

import com.be16_2nd.SmartFridge.food.domain.Category;
import com.be16_2nd.SmartFridge.food.domain.Food;
import com.be16_2nd.SmartFridge.food.domain.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FoodResDto {
    private String name;
    private String registeredName;
    private Integer quantity;
    private LocalDate storageStartDate;
    private LocalDateTime expirationDateTime;
    private Category category;
    private StorageType storageType;
    private Boolean shareable;
    private Boolean isTemp;

    public static FoodResDto fromEntity(Food food) {
        return FoodResDto.builder()
                .name(food.getName())
                .registeredName(food.getMember().getName()) // Member 연관관계 필요
                .quantity(food.getQuantity())
                .storageStartDate(food.getStorageStartDate())
                .expirationDateTime(food.getExpirationDateTime())
                .category(food.getCategory())
                .storageType(food.getStorageType())
                .shareable(food.getIsShared())
                .isTemp(food.getIsTemp())
                .build();
    }
}
