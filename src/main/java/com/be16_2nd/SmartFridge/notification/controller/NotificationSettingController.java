package com.be16_2nd.SmartFridge.notification.controller;

import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import com.be16_2nd.SmartFridge.notification.dto.NotificationSettingReqDto;
import com.be16_2nd.SmartFridge.notification.service.NotificationSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    @PostMapping("/setting")
    public ResponseEntity<?> setNotification(@RequestBody NotificationSettingReqDto notificationSettingReqDto) {
        notificationSettingService.setNotification(notificationSettingReqDto);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .status_code(HttpStatus.OK.value())
                        .status_message("알림 설정 완료")
                        .build(),
                HttpStatus.OK);

    }
}
