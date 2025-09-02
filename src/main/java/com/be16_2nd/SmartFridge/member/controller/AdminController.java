package com.be16_2nd.SmartFridge.member.controller;

import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import com.be16_2nd.SmartFridge.inquiryComment.dto.InquiryCommentCreateDto;
import com.be16_2nd.SmartFridge.member.service.AdminService;
import com.be16_2nd.SmartFridge.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final NotificationService notificationService;

    @GetMapping("/member/list")
    public ResponseEntity<?> findAllMember(){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(adminService.findAllMember())
                        .status_code(HttpStatus.OK.value())
                        .status_message("회원목록 조회 완료")
                        .build(),HttpStatus.OK);
    }

    @GetMapping("/member/detail/{memberEmail}")
    public ResponseEntity<?> memberDetail(@PathVariable String memberEmail){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(adminService.memberDetail(memberEmail))
                        .status_code(HttpStatus.OK.value())
                        .status_message("회원 상세조회 성공")
                        .build(),HttpStatus.OK);
    }

    @DeleteMapping("/member/delete/{memberEmail}")
    public ResponseEntity<?> deleteMember(@RequestBody String memberEmail){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(adminService.deleteMember(memberEmail))
                        .status_code(HttpStatus.OK.value())
                        .status_message("회원 삭제 완료")
                        .build(),HttpStatus.OK);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(adminService.dashbord())
                        .status_code(HttpStatus.OK.value())
                        .status_message("admin dashbord 조회 성공")
                        .build(),HttpStatus.OK);
    }

    @GetMapping("/inquiry/list")
    public ResponseEntity<?> findAllInquiry(){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(adminService.findAllInquiry())
                        .status_code(HttpStatus.OK.value())
                        .status_message("문의내역 전체조회 완료")
                        .build(),HttpStatus.OK);
    }

    @PostMapping("/inquiry/reply")
    public ResponseEntity<?> replyInquiry(@RequestParam Long inquiryId
            , @RequestBody InquiryCommentCreateDto inquiryCommentCreateDto){
        return new ResponseEntity<>(CommonDto.builder()
                .result(adminService.replyInquiry(inquiryCommentCreateDto, inquiryId))
                .status_code(HttpStatus.CREATED.value())
                .status_message("답변 등록 완료").build(), HttpStatus.CREATED);
    }

    @GetMapping("/inquiry/detail/{inquiryId}")
    public ResponseEntity<?> inquiryDetail(@PathVariable Long inquiryId){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(adminService.inquiryDetail(inquiryId))
                        .status_code(HttpStatus.OK.value())
                        .status_message("문의 상세조회 성공")
                        .build(),HttpStatus.OK);
    }

//    전체 알림 목록 조회
    @GetMapping("/notification/list")
    public ResponseEntity<?> findAllNotification(){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(adminService.findAllNotification())
                        .status_code(HttpStatus.OK.value())
                        .status_message("알림목록 조회 성공")
                        .build(),HttpStatus.OK);
    }

//    전체 알림(읽음) 목록 조회
    @GetMapping("/notification/read/list")
    public ResponseEntity<?> findAllReadNotification(){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(adminService.findAllReadNotification())
                        .status_code(HttpStatus.OK.value())
                        .status_message("읽은 알림목록 조회 성공")
                        .build(),HttpStatus.OK);
    }

//    전체 알림(안읽음) 목록 조회
    @GetMapping("/notification/unread/list")
    public ResponseEntity<?> findAllUnreadNotification(){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(adminService.findAllUnreadNotification())
                        .status_code(HttpStatus.OK.value())
                        .status_message("안읽은 알림목록 조회 성공")
                        .build(),HttpStatus.OK);
    }

//    @DeleteMapping("/member/delete/{}")
}