package com.be16_2nd.SmartFridge.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
Stomp 연결 된 세션 수 확인 목적
*/

@Slf4j
@Component
public class StompEventListener {

    private final Set<String> sessions = ConcurrentHashMap.newKeySet();

    @EventListener
    public void connectHandle(SessionConnectEvent event){
        log.info("stomp connect");
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.add(accessor.getSessionId());
        log.info("total session : " + sessions.size());
    }

    @EventListener
    public void disconnectHandle(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        sessions.remove(accessor.getSessionId());
        log.info("stomp disconnect");
        log.info("total session : " + sessions.size());
    }
}
