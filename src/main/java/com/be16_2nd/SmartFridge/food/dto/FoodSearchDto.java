package com.be16_2nd.SmartFridge.food.dto;

import com.be16_2nd.SmartFridge.food.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FoodSearchDto {
    private String foodName;
    private Category category;
    private FoodFilterType filterType;
    private Boolean isTemp;
    private Boolean viewAll = false;
}
