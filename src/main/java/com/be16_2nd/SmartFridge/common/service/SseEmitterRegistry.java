package com.be16_2nd.SmartFridge.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SseEmitterRegistry {

    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public void addSseEmitter(String email, SseEmitter sseEmitter) {
        emitterMap.put(email, sseEmitter);
        log.info("emitter 등록됨: {}", emitterMap);
        log.info("emitter 등록됨: {}", email);
    }

    public void removeEmitter(String email) {
        emitterMap.remove(email);
    }

    public SseEmitter getEmitter(String email) {
        return emitterMap.get(email);
    }

    public Set<String> getAllKeys() {
        return emitterMap.keySet();
    }

}
