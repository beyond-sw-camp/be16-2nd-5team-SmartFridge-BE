package com.be16_2nd.SmartFridge.fridge.controller;

import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeCreateDto;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeListDto;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeUpdateDto;
import com.be16_2nd.SmartFridge.fridge.service.FridgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PutMapping("/{fridgeId}/update")
    public ResponseEntity<?> update(@PathVariable Long fridgeId,
                                    @Valid @RequestBody FridgeUpdateDto fridgeUpdateDto) {
        Long id = fridgeService.update(fridgeId, fridgeUpdateDto);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(id)
                        .status_code(HttpStatus.OK.value())
                        .status_message(fridgeUpdateDto.getFridgeName() + " 냉장고가 수정되었습니다!")
                        .build(),
                HttpStatus.OK
        );
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

    @GetMapping("/{fridgeId}/detail")
    public ResponseEntity<?> fridgeDetailInfo(@PathVariable Long fridgeId){
        return new ResponseEntity<>(
                CommonDto
                        .builder()
                        .result(fridgeService.fridgeDetail(fridgeId))
                        .status_code(HttpStatus.OK.value())
                        .status_message("냉장고 정보조회완료")
                        .build(), HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<?> MyFridges() {
        List<FridgeListDto> myFridges = fridgeService.findMyFridges();
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(myFridges)
                        .status_code(HttpStatus.OK.value())
                        .status_message("냉장고 목록조회완료")
                        .build(),HttpStatus.OK);
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

    @DeleteMapping("/{fridgeId}")
    public ResponseEntity<?> deleteFridge(@PathVariable Long fridgeId){
        Long DeleteFridgeId = fridgeService.deleteFridge(fridgeId);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(DeleteFridgeId)
                        .status_code(HttpStatus.OK.value())
                        .status_message("냉장고 삭제완료")
                        .build(),HttpStatus.OK);
    }

    @DeleteMapping("/{fridgeId}/leave")
    public ResponseEntity<?> leaveFridge(@PathVariable Long fridgeId){
        Long LeaveFridgeId = fridgeService.leaveFridge(fridgeId);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(LeaveFridgeId)
                        .status_code(HttpStatus.OK.value())
                        .status_message("냉장고 나가기 완료")
                        .build(),HttpStatus.OK);
    }


}
