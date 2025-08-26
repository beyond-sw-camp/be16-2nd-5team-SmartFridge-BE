package com.be16_2nd.SmartFridge.notification.controller;

import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import com.be16_2nd.SmartFridge.common.service.FridgeAccessValidator;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.service.MemberService;
import com.be16_2nd.SmartFridge.notification.domain.NotificationType;
import com.be16_2nd.SmartFridge.notification.dto.NotificationReadReqDto;
import com.be16_2nd.SmartFridge.notification.dto.NotificationResDto;
import com.be16_2nd.SmartFridge.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("fridge/{fridgeId}/notification")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final MemberService memberService;

    // 사용자 별 알림 목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> notificationList(@PathVariable Long fridgeId,
                                              @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                              @RequestParam(required = false) NotificationType notificationType) {
        Page<NotificationResDto> notificationResDtoPage = notificationService.findNotificationList(fridgeId
                                                                        , notificationType, pageable);
        return new ResponseEntity<>(CommonDto.builder()
                .result(notificationResDtoPage)
                .status_code(HttpStatus.OK.value())
                .status_message("알림 목록 조회 성공")
                .build()
                , HttpStatus.OK);
    }

    // 알림 삭제
    @DeleteMapping("/delete/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return new ResponseEntity<>(CommonDto.builder()
                .result(null)
                .status_code(HttpStatus.OK.value())
                .status_message("알림 삭제 성공")
                .build()
                , HttpStatus.OK);
    }
    
    // 알림 읽음 처리
    @PatchMapping("/isRead")
    public ResponseEntity<?> readNotification(@RequestBody List<NotificationReadReqDto> notificationReadReqDtoList) {
        notificationService.readNotification(notificationReadReqDtoList);
        return new ResponseEntity<>(CommonDto.builder()
                .result(null)
                .status_code(HttpStatus.OK.value())
                .status_message("알림 읽음 처리 성공")
                .build()
                , HttpStatus.OK);
    }
}
