package com.be16_2nd.SmartFridge.food.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FoodStatResDto {
    private long totalCount;      // 총 식품 수
    private long freshCount;      // 신선 식품 수
    private long expiringSoonCount; // 유통기한 임박 식품 수
    private long expiredCount;
}
