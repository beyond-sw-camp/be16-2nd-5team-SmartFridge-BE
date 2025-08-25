package com.be16_2nd.SmartFridge.notification.service;

import com.be16_2nd.SmartFridge.common.dto.SseMessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
//@RequiredArgsConstructor
public class NotificationPublisher {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public NotificationPublisher(@Qualifier("ssePubSub") RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(Long fridgeId, String senderEmail, String receiverEmail, String contents, String type) {
        SseMessageDTO sseMessageDTO = SseMessageDTO.builder()
                .type(type)
                .fridgeId(fridgeId)
                .senderEmail(senderEmail)
                .receiverEmail(receiverEmail)
                .contents(contents)
                .build();

        String data;
        try {
            data = objectMapper.writeValueAsString(sseMessageDTO);
            log.info("Redis 채널로 알림 발행. 수신자: {}", receiverEmail);
            redisTemplate.convertAndSend("sse-channel", data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
