package com.be16_2nd.SmartFridge.inquiry.controller;

import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import com.be16_2nd.SmartFridge.inquiry.dto.InquiryCreateDto;
import com.be16_2nd.SmartFridge.inquiry.dto.InquiryResDto;
import com.be16_2nd.SmartFridge.inquiry.dto.InquiryUpdateDto;
import com.be16_2nd.SmartFridge.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inquiry")
public class InquiryController {

    private final InquiryService inquiryService;

//    문의글 작성하기
    @PostMapping("/create")
    public ResponseEntity<?> create(@ModelAttribute InquiryCreateDto inquiryCreateDto){
        return new ResponseEntity<>(CommonDto.builder()
                .result(inquiryService.create(inquiryCreateDto))
                .status_code(HttpStatus.CREATED.value())
                .status_message("문의 등록 완료").build(), HttpStatus.CREATED);
    }


//    내 문의 내역 조회
    @GetMapping("/myInquiry")
    public ResponseEntity<?> myInquiry(){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(inquiryService.findMyInquiry())
                        .status_code(HttpStatus.OK.value())
                        .status_message("내 문의내역 조회 성공")
                        .build(),HttpStatus.OK);
    }

//    문의 수정하기
    @PatchMapping("/update/{inquiryId}")
    public ResponseEntity<?> updateInquiry(@PathVariable Long inquiryId,
                                           @ModelAttribute InquiryUpdateDto inquiryUpdateDto){

        Long id = inquiryService.updateInquiry(inquiryId, inquiryUpdateDto);

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(id)
                        .status_code(HttpStatus.OK.value())
                        .status_message("문의 내역 수정 완료")
                        .build(),HttpStatus.OK);
    }
//    문의 삭제
    @DeleteMapping("/delete/{inquiryId}")
    public ResponseEntity<?> deleteInquiry(@PathVariable Long inquiryId){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(inquiryService.deleteInquiry(inquiryId))
                        .status_code(HttpStatus.OK.value())
                        .status_message("문의 삭제 완료")
                        .build(),HttpStatus.OK);
    }


}
