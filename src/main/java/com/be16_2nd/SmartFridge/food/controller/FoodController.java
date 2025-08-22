package com.be16_2nd.SmartFridge.food.controller;

import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import com.be16_2nd.SmartFridge.food.dto.*;
import com.be16_2nd.SmartFridge.food.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fridge/{fridgeId}/food")
public class FoodController {

    private final FoodService foodService;

    @PostMapping("/register")
    public ResponseEntity<?> registerFood(@PathVariable("fridgeId") Long fridgeId,
                                          @RequestBody @Valid FoodCreateDto foodCreateDto) {
        FoodResDto foodResponse = foodService.registerFood(fridgeId, foodCreateDto);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(foodResponse)
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("식품 등록 완료")
                        .build(),
                HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<?> foodList(@PathVariable("fridgeId") Long fridgeId,
                                         Pageable pageable,
                                         @ModelAttribute FoodSearchDto foodSearchDto,
                                         @RequestParam(required = false) Boolean isShared) {
        Page<FoodResDto> foodResDtoList = foodService.getFoodsByRole(fridgeId, pageable, foodSearchDto, isShared);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(foodResDtoList)
                        .status_code(HttpStatus.OK.value())
                        .status_message("식품 목록 조회 완료")
                        .build(),
                HttpStatus.OK);
    }

    @GetMapping("/mylist")
    public ResponseEntity<?> myFoodList(@PathVariable("fridgeId") Long fridgeId,
                                           Pageable pageable,
                                           @ModelAttribute FoodSearchDto foodSearchDto,
                                           @RequestParam(required = false) Boolean isShared) {
        Page<FoodResDto> foodResDtoList = foodService.getFoodsByRole(fridgeId, pageable, foodSearchDto, isShared);

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(foodResDtoList)
                        .status_code(HttpStatus.OK.value())
                        .status_message("내 식품 목록 조회 완료")
                        .build(),
                HttpStatus.OK);
    }

    @PutMapping("/update/{foodId}")
    public ResponseEntity<?> updateFood(@PathVariable("fridgeId") Long fridgeId,
                                        @PathVariable("foodId") Long foodId,
                                        @RequestBody @Valid FoodUpdateDto foodUpdateDto) {
        FoodResDto foodResponse = foodService.updateFood(fridgeId, foodId, foodUpdateDto);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(foodResponse)
                        .status_code(HttpStatus.OK.value())
                        .status_message("식품 정보 수정 완료")
                        .build(),
                HttpStatus.OK);
    }

    @DeleteMapping("/delete/{foodId}")
    public ResponseEntity<?> deleteFood(@PathVariable("fridgeId") Long fridgeId,
                                        @PathVariable("foodId") Long foodId) {
        foodService.deleteFood(fridgeId, foodId);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result("SUCCESS")
                        .status_code(HttpStatus.OK.value())
                        .status_message("식품이 성공적으로 삭제되었습니다.")
                        .build(),
                HttpStatus.OK);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> foodStats(@PathVariable Long fridgeId) {
        FoodStatResDto stats = foodService.foodStats(fridgeId);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(stats)
                        .status_code(HttpStatus.OK.value())
                        .status_message("식품통계")
                        .build(),
                HttpStatus.OK);
    }
}