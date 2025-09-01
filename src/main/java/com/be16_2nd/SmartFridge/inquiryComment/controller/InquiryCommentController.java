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

    @Transactional(readOnly = true)
    public ResponseEntity<?> myInquiryComment(){
        return new ResponseEntity(
                CommonDto.builder()
                        .result(inquiryCommentService.findMyInquiryComment())
                        .status_code(HttpStatus.OK.value())
                        .status_message("문의 답변내역 조회 성공")
                        .build(),HttpStatus.OK);
    }
}
