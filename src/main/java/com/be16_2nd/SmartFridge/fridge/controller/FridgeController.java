package com.be16_2nd.SmartFridge.fridge.controller;

import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeCreateDto;
import com.be16_2nd.SmartFridge.fridge.service.FridgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fridge")
@Slf4j
public class FridgeController {

    private final FridgeService fridgeService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody @Valid FridgeCreateDto fridgeCreateDto){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(fridgeService.create(fridgeCreateDto))
                        .status_code(HttpStatus.CREATED.value())
                        .status_message(fridgeCreateDto.getFridgeName() + "냉장고가 생성되었습니다!")
                        .build(), HttpStatus.CREATED);
    }

    @GetMapping("/{fridgeId}")
    public ResponseEntity<?> fridgeDetail(@PathVariable Long fridgeId){
        return new ResponseEntity<>(
                CommonDto
                        .builder()
                        .result(fridgeService.findByFridgeId(fridgeId))
                        .status_code(HttpStatus.OK.value())
                        .status_message("OK")
                        .build(), HttpStatus.OK);
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinFridge(@RequestParam("code") @Valid String inviteCode) {
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(fridgeService.joinFridge(inviteCode))
                        .status_code(HttpStatus.OK.value())
                        .status_message("냉장고 가입 완료!")
                        .build(),HttpStatus.OK);
    }


}
