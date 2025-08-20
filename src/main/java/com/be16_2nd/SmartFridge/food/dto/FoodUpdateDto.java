package com.be16_2nd.SmartFridge.food.dto;

import com.be16_2nd.SmartFridge.food.domain.Category;
import com.be16_2nd.SmartFridge.food.domain.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FoodUpdateDto {
    private String name;
    private Integer quantity;
    private StorageType storageType;
    private Category category;

    private LocalDate expirationDate;
    private Integer expirationHour;
    private Integer expirationMinute;

    private String memo;
    private Boolean shareable;
    private Boolean isTemp;
}
