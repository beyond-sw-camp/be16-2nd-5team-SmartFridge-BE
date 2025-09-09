package com.be16_2nd.SmartFridge.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/*
* stomp 설정 파일
*/

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocket implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    // Stomp 연결
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect/chat")
                .setAllowedOrigins("https://www.smartfridge.cloud")
                .withSockJS();
    }

    // 송수신 패턴
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/publish/chat");
        registry.enableSimpleBroker("/topic/chat");
    }

    // stomp 인터셉터
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}
