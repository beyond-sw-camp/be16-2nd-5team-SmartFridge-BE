package com.be16_2nd.SmartFridge.inquiry.controller;

import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import com.be16_2nd.SmartFridge.inquiry.dto.InquiryCreateDto;
import com.be16_2nd.SmartFridge.inquiry.dto.InquiryResDto;
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

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inquiry")
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody InquiryCreateDto inquiryCreateDto){
        return new ResponseEntity<>(CommonDto.builder()
                .result(inquiryService.create(inquiryCreateDto))
                .status_code(HttpStatus.CREATED.value())
                .status_message("문의 등록 완료").build(), HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<?> inquiryList() {
        List<InquiryResDto> inquiryPage = inquiryService.getInquiryList();

        return new ResponseEntity<>(CommonDto.builder()
                .result(inquiryPage)
                .status_code(HttpStatus.OK.value())
                .status_message("문의 조회 성공").build(), HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAll(){
        return null;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> myInquiry(){
        return null;
    }
}
