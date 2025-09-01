package com.be16_2nd.SmartFridge.common.controller;

import com.be16_2nd.SmartFridge.common.service.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController {

    private final SseEmitterRegistry sseEmitterRegistry;

    @GetMapping("/connect/notification")
    public SseEmitter subscribe() {
        log.info("현재 registry 상태: {}", sseEmitterRegistry.getAllKeys());
        // SSE 연결 시간: 4시간
        SseEmitter sseEmitter = new SseEmitter(14400 * 60 * 1000L);

        // JWT 인증 정보에서 사용자 이메일 가져오기
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("SSE 연결 시도됨. 사용자 email: {}", email);

//        // 레지스트리에 추가
        sseEmitterRegistry.addSseEmitter(email, sseEmitter);
        log.info("현재 registry 상태: {}", sseEmitterRegistry.getAllKeys());

        try {
            // 최초 연결 이벤트 전송
            sseEmitter.send(SseEmitter.event()
                    .name("connect")
                    .data("연결 완료"));
            log.info("현재 registry 상태: {}", sseEmitterRegistry.getAllKeys());

        } catch (IOException e) {
            // 최초 연결 시 전송 실패
            log.error("SSE 초기 연결 실패: {}", email, e);
            sseEmitter.complete();
        }

        return sseEmitter;
    }


    @GetMapping("/disconnect/notification")
    public void unSubscribe() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        sseEmitterRegistry.removeEmitter(email);
    }
}
