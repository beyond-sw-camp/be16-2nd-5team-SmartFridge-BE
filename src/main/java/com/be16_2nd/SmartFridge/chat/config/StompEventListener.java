package com.be16_2nd.SmartFridge.chat.config;

import com.be16_2nd.SmartFridge.chat.service.ChatPresence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
Stomp 연결 된 세션 수 확인, 연결 종료 시 지금 보고 있는 채팅방 정리
*/

@Slf4j
@Component
@RequiredArgsConstructor
public class StompEventListener {

    private final Set<String> sessions = ConcurrentHashMap.newKeySet();
    private final ChatPresence chatPresence;

    @EventListener
    public void connectHandle(SessionConnectEvent event){
        log.info("stomp connect");
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.add(accessor.getSessionId());
        log.info("total session : " + sessions.size());
    }

    // DISCONNECT 프레임 없이 끊겨도(탭 닫기, 네트워크 끊김) 발생한다
    @EventListener
    public void disconnectHandle(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.remove(event.getSessionId());
        chatPresence.disconnect(accessor.getSessionAttributes(), event.getSessionId());
        log.info("stomp disconnect");
        log.info("total session : " + sessions.size());
    }
}
