package com.be16_2nd.SmartFridge.chat.service;

import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * 지금 채팅방을 보고 있는 사용자 (서버 여러 대가 공유하도록 Redis에 저장)
 * - key   : chat:presence:{TYPE}:{roomId}  (Hash)
 * - field : {STOMP sessionId}:{subscriptionId}  -> value : 회원 이메일
 *   구독 단위로 저장하므로, 같은 사용자가 탭 두 개로 보다가 하나를 닫아도 나머지 탭 기준으로 계속 접속 중이다
 * - 서버가 비정상 종료되면 DISCONNECT가 오지 않아 항목이 남으므로, 방 key에 TTL을 두고 입장할 때마다 갱신한다
 */
@Component
public class ChatPresence {

    static final Duration TTL = Duration.ofHours(12);
    private static final String KEY_PREFIX = "chat:presence:";
    private static final String SUBSCRIPTIONS_ATTR = "chatSubscriptions";

    private final StringRedisTemplate redisTemplate;

    public ChatPresence(@Qualifier("chatPubSub") StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 구독 시작: Redis에 기록하고, 연결(세션) 속성에 구독 id -> 방 정보를 남긴다
    public void subscribe(Map<String, Object> sessionAttributes, String sessionId, String subscriptionId,
                          ChatRoomType type, Long roomId, String email) {
        String key = key(type, roomId);
        redisTemplate.opsForHash().put(key, field(sessionId, subscriptionId), email);
        redisTemplate.expire(key, TTL);
        subscriptions(sessionAttributes).put(subscriptionId, new Room(type, roomId));
    }

    // 구독 해제: 구독 id로 방을 찾으므로 클라이언트가 방 정보를 따로 보낼 필요가 없다
    public void unsubscribe(Map<String, Object> sessionAttributes, String sessionId, String subscriptionId) {
        Room room = subscriptions(sessionAttributes).remove(subscriptionId);
        if (room != null) {
            redisTemplate.opsForHash().delete(key(room.type(), room.roomId()), field(sessionId, subscriptionId));
        }
    }

    // 연결 종료(탭 닫기, 네트워크 끊김 포함): 이 연결의 구독을 모두 정리한다
    public void disconnect(Map<String, Object> sessionAttributes, String sessionId) {
        if (sessionAttributes == null) {
            return;
        }
        for (String subscriptionId : List.copyOf(subscriptions(sessionAttributes).keySet())) {
            unsubscribe(sessionAttributes, sessionId, subscriptionId);
        }
    }

    public Set<String> onlineEmails(ChatRoomType type, Long roomId) {
        Set<String> emails = new HashSet<>();
        for (Object value : redisTemplate.opsForHash().values(key(type, roomId))) {
            emails.add((String) value);
        }
        return emails;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Room> subscriptions(Map<String, Object> sessionAttributes) {
        return (Map<String, Room>) sessionAttributes.computeIfAbsent(SUBSCRIPTIONS_ATTR, k -> new ConcurrentHashMap<String, Room>());
    }

    private record Room(ChatRoomType type, Long roomId) {}

    static String key(ChatRoomType type, Long roomId) {
        return KEY_PREFIX + type.name() + ":" + roomId;
    }

    private static String field(String sessionId, String subscriptionId) {
        return sessionId + ":" + subscriptionId;
    }
}
