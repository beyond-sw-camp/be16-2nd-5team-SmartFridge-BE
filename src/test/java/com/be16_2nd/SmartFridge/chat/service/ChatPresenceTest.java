package com.be16_2nd.SmartFridge.chat.service;

import com.be16_2nd.SmartFridge.chat.config.StompEventListener;
import com.be16_2nd.SmartFridge.chat.domain.ChatParticipant;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.dto.ChatMessageDto;
import com.be16_2nd.SmartFridge.chat.dto.ChatRoomCreateDto;
import com.be16_2nd.SmartFridge.chat.repository.ChatRoomRepository;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeCreateDto;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeRepository;
import com.be16_2nd.SmartFridge.fridge.service.FridgeService;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import com.be16_2nd.SmartFridge.member.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@Transactional
class ChatPresenceTest {

    // DB에 없는 방 id (Redis만 쓰는 테스트용)
    static final long ROOM_1 = 990_001L;
    static final long ROOM_2 = 990_002L;

    @Autowired ChatPresence chatPresence;
    @Autowired StompEventListener stompEventListener;
    @Autowired @Qualifier("chatPubSub") StringRedisTemplate redisTemplate;
    @Autowired MemberService memberService;
    @Autowired FridgeService fridgeService;
    @Autowired FridgeRepository fridgeRepository;
    @Autowired ChatService chatService;
    @Autowired ChatRoomRepository chatRoomRepository;

    final List<String> usedKeys = new ArrayList<>(List.of(
            ChatPresence.key(ChatRoomType.PURCHASE, ROOM_1),
            ChatPresence.key(ChatRoomType.MANAGER, ROOM_2)));

    @AfterEach
    void tearDown() {
        redisTemplate.delete(usedKeys);
        SecurityContextHolder.clearContext();
    }

    @Test
    void 같은_사용자가_탭_두개로_보다가_하나를_닫아도_접속_중으로_남는다() {
        Map<String, Object> tab1 = new HashMap<>();
        Map<String, Object> tab2 = new HashMap<>();
        chatPresence.subscribe(tab1, "s1", "sub-0", ChatRoomType.PURCHASE, ROOM_1, "a@p.com");
        chatPresence.subscribe(tab2, "s2", "sub-0", ChatRoomType.PURCHASE, ROOM_1, "a@p.com");

        chatPresence.unsubscribe(tab1, "s1", "sub-0");
        assertThat(chatPresence.onlineEmails(ChatRoomType.PURCHASE, ROOM_1)).containsExactly("a@p.com");

        chatPresence.unsubscribe(tab2, "s2", "sub-0");
        assertThat(chatPresence.onlineEmails(ChatRoomType.PURCHASE, ROOM_1)).isEmpty();
    }

    @Test
    void 연결이_끊기면_그_연결의_모든_구독이_정리된다() {
        Map<String, Object> session = new HashMap<>();
        chatPresence.subscribe(session, "s1", "sub-0", ChatRoomType.PURCHASE, ROOM_1, "a@p.com");
        chatPresence.subscribe(session, "s1", "sub-1", ChatRoomType.MANAGER, ROOM_2, "a@p.com");
        chatPresence.subscribe(new HashMap<>(), "s2", "sub-0", ChatRoomType.PURCHASE, ROOM_1, "b@p.com");

        // DISCONNECT 프레임 없이 끊긴 경우에도 Spring이 보내는 이벤트
        stompEventListener.disconnectHandle(disconnectEvent("s1", session));

        assertThat(chatPresence.onlineEmails(ChatRoomType.PURCHASE, ROOM_1)).containsExactly("b@p.com");
        assertThat(chatPresence.onlineEmails(ChatRoomType.MANAGER, ROOM_2)).isEmpty();
    }

    @Test
    void 모르는_구독_해제나_속성_없는_연결_종료는_무시된다() {
        assertThatCode(() -> {
            chatPresence.unsubscribe(new HashMap<>(), "s1", "unknown");
            chatPresence.disconnect(null, "s1");
        }).doesNotThrowAnyException();
    }

    @Test
    void 방에_입장할_때_TTL이_설정된다() {
        chatPresence.subscribe(new HashMap<>(), "s1", "sub-0", ChatRoomType.PURCHASE, ROOM_1, "a@p.com");

        Long ttl = redisTemplate.getExpire(ChatPresence.key(ChatRoomType.PURCHASE, ROOM_1));
        assertThat(ttl).isPositive().isLessThanOrEqualTo(ChatPresence.TTL.toSeconds());
    }

    @Test
    void 지금_방을_보고_있는_참여자만_새_메시지가_읽음_처리된다() {
        memberService.socialLogin(SocialType.GOOGLE, "p-a", "a@p.com", true, "회원A", null);
        memberService.socialLogin(SocialType.GOOGLE, "p-b", "b@p.com", true, "회원B", null);
        memberService.socialLogin(SocialType.GOOGLE, "p-c", "c@p.com", true, "회원C", null);
        login("a@p.com");
        Long fridgeId = fridgeService.create(FridgeCreateDto.builder().fridgeName("접속냉장고").build()).getFridgeId();
        String inviteCode = fridgeRepository.findById(fridgeId).orElseThrow().getInviteCode();
        Long roomId = chatService.createPurchaseChatRoom(ChatRoomCreateDto.builder()
                .fridgeId(fridgeId).title("공구").maxParticipants(3).build());
        usedKeys.add(ChatPresence.key(ChatRoomType.PURCHASE, roomId));
        for (String email : List.of("b@p.com", "c@p.com")) {
            login(email);
            fridgeService.joinFridge(inviteCode);
            chatService.addParticipantToPurchaseChat(roomId);
        }

        // b만 방을 보고 있다
        chatPresence.subscribe(new HashMap<>(), "s-b", "sub-0", ChatRoomType.PURCHASE, roomId, "b@p.com");
        Long messageId = chatService.saveMessage(roomId, ChatRoomType.PURCHASE, "a@p.com",
                ChatMessageDto.builder().message("안녕").build()).getId();

        Map<String, Long> cursors = new HashMap<>();
        for (ChatParticipant p : chatRoomRepository.findById(roomId).orElseThrow().getParticipants()) {
            cursors.put(p.getMember().getEmail(), p.getLastReadMessageId());
        }
        assertThat(cursors.get("a@p.com")).isEqualTo(messageId);
        assertThat(cursors.get("b@p.com")).isEqualTo(messageId);
        assertThat(cursors.get("c@p.com")).isLessThan(messageId);
    }

    private SessionDisconnectEvent disconnectEvent(String sessionId, Map<String, Object> sessionAttributes) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionId(sessionId);
        accessor.setSessionAttributes(sessionAttributes);
        return new SessionDisconnectEvent(this,
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders()), sessionId, CloseStatus.NORMAL);
    }

    private void login(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }
}
