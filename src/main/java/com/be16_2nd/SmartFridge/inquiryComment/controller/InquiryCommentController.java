package com.be16_2nd.SmartFridge.inquiryComment.controller;

import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import com.be16_2nd.SmartFridge.inquiryComment.dto.InquiryCommentCreateDto;
import com.be16_2nd.SmartFridge.inquiryComment.service.InquiryCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inquirycomment")
public class InquiryCommentController {

    private final InquiryCommentService inquiryCommentService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestParam Long inquiryId
            , @RequestBody InquiryCommentCreateDto inquiryCommentCreateDto){
        return new ResponseEntity<>(CommonDto.builder()
                .result(inquiryCommentService.create(inquiryCommentCreateDto, inquiryId))
                .status_code(HttpStatus.CREATED.value())
                .status_message("답변 등록 완료").build(), HttpStatus.CREATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAll(){
        return null;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> myInquiryComment(){
        return null;
    }
}
