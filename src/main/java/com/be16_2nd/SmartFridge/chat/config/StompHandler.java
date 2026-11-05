package com.be16_2nd.SmartFridge.chat.config;

import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.service.ManagerChatRoomLifecycle;
import com.be16_2nd.SmartFridge.chat.service.Validator.ChatRoomParticipantValidator;
import com.be16_2nd.SmartFridge.chat.service.Validator.TokenValidator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* Stomp 인터셉터
* - CONNECT : 토큰을 검증하고 사용자를 세션에 묶는다 (이후 프레임은 세션 사용자 기준)
* - SUBSCRIBE : 구독 대상 채팅방의 참여자인지 확인
* - SEND : 인증된 세션만 허용 (참여자 확인은 ChatService.saveMessage)
*/

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private static final Pattern SUBSCRIBE_DESTINATION = Pattern.compile("^/topic/chat/(MANAGER|PURCHASE)/(\\d+)$");

    private final TokenValidator tokenValidator;
    private final ChatRoomParticipantValidator chatRoomParticipantValidator;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        switch (accessor.getCommand()) {
            case CONNECT -> connect(accessor);
            case SUBSCRIBE -> subscribe(accessor);
            case SEND -> requireUser(accessor);
            case UNSUBSCRIBE -> unsubscribe(accessor);
            default -> { }
        }
        return message;
    }

    private void connect(StompHeaderAccessor accessor) {
        Claims claims = tokenValidator.validateToken(accessor.getFirstNativeHeader("Authorization"));
        String email = claims.getSubject();
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority("ROLE_" + claims.get("role")))));
        accessor.getSessionAttributes().put("email", email);
        log.info("stomp connect 토큰 검증 완료");
    }

    private void subscribe(StompHeaderAccessor accessor) {
        String email = requireUser(accessor);
        Matcher matcher = SUBSCRIBE_DESTINATION.matcher(Objects.requireNonNullElse(accessor.getDestination(), ""));
        if (!matcher.matches()) {
            throw new AccessDeniedException("잘못된 구독 경로입니다.");
        }
        ChatRoomType roomType = ChatRoomType.valueOf(matcher.group(1));
        Long roomId = Long.parseLong(matcher.group(2));

        // 채팅방 참여자 확인
        chatRoomParticipantValidator.validateParticipant(email, roomId, roomType);
        // 현재 채팅방을 보고 있는 사용자에 추가
        ManagerChatRoomLifecycle.onlineMembers(roomType)
                .computeIfAbsent(roomId, k -> new HashSet<>())
                .add(email);
    }

    // 구독 끊을 경우 현재 접속 중인 사용자 제거
    private void unsubscribe(StompHeaderAccessor accessor) {
        String email = (String) accessor.getSessionAttributes().get("email");
        String roomType = Objects.requireNonNull(accessor.getNativeHeader("roomType")).get(0);
        Long roomId = Long.parseLong(Objects.requireNonNull(accessor.getNativeHeader("roomId")).get(0));
        if (roomType.equals("manager")) {
            ManagerChatRoomLifecycle.ManagerRoomParticipants.get(roomId).remove(email);
        } else if (roomType.equals("purchase")) {
            ManagerChatRoomLifecycle.PurchaseRoomParticipants.get(roomId).remove(email);
        }
    }

    // CONNECT 때 세션에 묶인 사용자 (없으면 거부)
    private String requireUser(StompHeaderAccessor accessor) {
        Principal user = accessor.getUser();
        if (user == null) {
            throw new AccessDeniedException("인증되지 않은 STOMP 세션입니다.");
        }
        return user.getName();
    }
}
