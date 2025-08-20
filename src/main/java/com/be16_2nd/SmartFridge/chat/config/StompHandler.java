package com.be16_2nd.SmartFridge.chat.config;

import com.be16_2nd.SmartFridge.chat.service.ManagerChatRoomLifecycle;
import com.be16_2nd.SmartFridge.chat.service.ChatService;
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
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;

/*
* Stomp 인터셉터
*/

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final ChatService chatService;
    private final TokenValidator tokenValidator;
    private final ChatRoomParticipantValidator chatRoomParticipantValidator;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // CONNECT 요청 시
        if(StompCommand.CONNECT == accessor.getCommand()) {
            log.info("stomp connect 요청");
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            tokenValidator.validateToken(bearerToken);
            log.info("토큰 검증 완료");
        }
        // SUBSCRIBE 요청 시
        if(StompCommand.SUBSCRIBE == accessor.getCommand()) {
            log.info("stomp subscribe 요청");
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            Claims claims = tokenValidator.validateToken(bearerToken);

            String email = claims.getSubject();
            String roomType = accessor.getDestination().split("/")[3];
            Long roomId = Long.parseLong(accessor.getDestination().split("/")[4]);

            accessor.getSessionAttributes().put("email", email);

            // 1대1 채팅방
            if (roomType.equals("MANAGER")) {
                // 채팅방 참여자 확인
                chatRoomParticipantValidator.validateManagerRoomParticipant(email, roomId);
                // 현재 채팅방 참여자에 추가
                ManagerChatRoomLifecycle.ManagerRoomParticipants
                        .computeIfAbsent(roomId, k -> new HashSet<>())
                        .add(email);
            }
            // 공동 구매 채팅방
            else {
                // 채팅방 참여자 확인
                chatRoomParticipantValidator.validatePurchaseRoomParticipant(email, roomId);
                // 현재 채팅방 참여자에 추가
                ManagerChatRoomLifecycle.PurchaseRoomParticipants
                        .computeIfAbsent(roomId, k -> new HashSet<>())
                        .add(email);
            }
        }

        // 구독 끊을 경우 현재 접속 중인 사용자 제거
        if(StompCommand.UNSUBSCRIBE == accessor.getCommand()){
            log.info("subscribe해제");

            String email = (String) accessor.getSessionAttributes().get("email");
            String roomType = Objects.requireNonNull(accessor.getNativeHeader("id")).get(0).split("/")[3];
            Long roomId = Long.parseLong(Objects.requireNonNull(accessor.getNativeHeader("id")).get(0).split("/")[4]);
//             현재 채팅방 참여자 목록에서 제거
            if(roomType.equals("MANAGER")){
                ManagerChatRoomLifecycle.ManagerRoomParticipants.get(roomId).remove(email);
            }else if(roomType.equals("PURCHASE")){
                ManagerChatRoomLifecycle.PurchaseRoomParticipants.get(roomId).remove(email);
            }
        }
        return message;
    }
}

