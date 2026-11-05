package com.be16_2nd.SmartFridge.chat.service;

import com.be16_2nd.SmartFridge.chat.config.StompHandler;
import com.be16_2nd.SmartFridge.chat.domain.ChatMessage;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.domain.JoinStatus;
import com.be16_2nd.SmartFridge.chat.dto.ChatMessageDto;
import com.be16_2nd.SmartFridge.chat.dto.ChatRoomCreateDto;
import com.be16_2nd.SmartFridge.chat.repository.PurchasePostRepository;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeCreateDto;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeRepository;
import com.be16_2nd.SmartFridge.fridge.service.FridgeService;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import com.be16_2nd.SmartFridge.member.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ChatSecurityTest {

    @Autowired MemberService memberService;
    @Autowired FridgeService fridgeService;
    @Autowired FridgeRepository fridgeRepository;
    @Autowired ChatService chatService;
    @Autowired PurchasePostRepository purchasePostRepository;
    @Autowired StompHandler stompHandler;
    @Autowired ChatPresence chatPresence;
    @Autowired @Qualifier("chatPubSub") StringRedisTemplate redisTemplate;

    Long fridgeId;
    Long purchaseRoomId;

    @BeforeEach
    void setUp() {
        memberService.socialLogin(SocialType.GOOGLE, "s-m", "manager@sec.com", true, "관리자", null);
        memberService.socialLogin(SocialType.GOOGLE, "s-a", "a@sec.com", true, "회원A", null);
        memberService.socialLogin(SocialType.GOOGLE, "s-b", "b@sec.com", true, "회원B", null);
        memberService.socialLogin(SocialType.GOOGLE, "s-x", "outsider@sec.com", true, "외부인", null);

        login("manager@sec.com");
        fridgeId = fridgeService.create(FridgeCreateDto.builder().fridgeName("보안냉장고").build()).getFridgeId();
        String inviteCode = fridgeRepository.findById(fridgeId).orElseThrow().getInviteCode();
        login("a@sec.com");
        fridgeService.joinFridge(inviteCode);
        login("b@sec.com");
        fridgeService.joinFridge(inviteCode);

        login("a@sec.com");
        purchaseRoomId = chatService.createPurchaseChatRoom(ChatRoomCreateDto.builder()
                .fridgeId(fridgeId).title("공구").maxParticipants(3).build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        redisTemplate.delete(ChatPresence.key(ChatRoomType.PURCHASE, purchaseRoomId));
    }

    @Test
    void 메시지_발신자는_본문의_senderEmail이_아니라_인증된_사용자다() {
        ChatMessage saved = chatService.saveMessage(purchaseRoomId, ChatRoomType.PURCHASE, "a@sec.com",
                ChatMessageDto.builder().senderEmail("manager@sec.com").message("위조 시도").build());

        assertThat(saved.getSender().getEmail()).isEqualTo("a@sec.com");
    }

    @Test
    void 메시지에_외부_이미지_URL을_넣을_수_없다() {
        assertThatThrownBy(() -> chatService.saveMessage(purchaseRoomId, ChatRoomType.PURCHASE, "a@sec.com",
                ChatMessageDto.builder().imageUrls(List.of("https://evil.example.com/x.png")).build()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 참여하지_않은_방에는_메시지를_보낼_수_없다() {
        assertThatThrownBy(() -> chatService.saveMessage(purchaseRoomId, ChatRoomType.PURCHASE, "b@sec.com",
                ChatMessageDto.builder().message("끼어들기").build()))
                .isInstanceOf(AuthorizationServiceException.class);
    }

    @Test
    void 다른_냉장고_사용자는_공동구매방_정보와_참여자를_볼_수_없다() {
        login("outsider@sec.com");
        assertThatThrownBy(() -> chatService.getRoomInfo(purchaseRoomId)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> chatService.getParticipants(purchaseRoomId)).isInstanceOf(AccessDeniedException.class);

        login("b@sec.com");
        assertThat(chatService.getParticipants(purchaseRoomId)).hasSize(1);
    }

    @Test
    void 공동구매_완료는_방장만_할_수_있다() {
        login("b@sec.com");
        chatService.addParticipantToPurchaseChat(purchaseRoomId);
        assertThatThrownBy(() -> chatService.completePurchase(purchaseRoomId)).isInstanceOf(AccessDeniedException.class);

        login("a@sec.com");
        chatService.completePurchase(purchaseRoomId);
        assertThat(purchasePostRepository.findByChatRoomId(purchaseRoomId).orElseThrow().getJoinStatus())
                .isEqualTo(JoinStatus.CLOSE);
    }

    @Test
    void 모집이_끝난_공동구매에는_참여할_수_없다() {
        chatService.completePurchase(purchaseRoomId); // a(방장)가 마감

        login("b@sec.com");
        assertThatThrownBy(() -> chatService.addParticipantToPurchaseChat(purchaseRoomId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void STOMP_인증되지_않은_세션의_SEND와_SUBSCRIBE는_거부된다() {
        assertThatThrownBy(() -> stompHandler.preSend(frame(StompCommand.SEND, "/publish/chat/PURCHASE/" + purchaseRoomId, null), null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> stompHandler.preSend(frame(StompCommand.SUBSCRIBE, "/topic/chat/PURCHASE/" + purchaseRoomId, null), null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void STOMP_참여하지_않은_방이나_잘못된_경로는_구독할_수_없다() {
        assertThatThrownBy(() -> stompHandler.preSend(frame(StompCommand.SUBSCRIBE, "/topic/chat/PURCHASE/" + purchaseRoomId, "b@sec.com"), null))
                .isInstanceOf(AuthorizationServiceException.class);
        assertThatThrownBy(() -> stompHandler.preSend(frame(StompCommand.SUBSCRIBE, "/topic/chat/OTHER/" + purchaseRoomId, "a@sec.com"), null))
                .isInstanceOf(AccessDeniedException.class);

        stompHandler.preSend(frame(StompCommand.SUBSCRIBE, "/topic/chat/PURCHASE/" + purchaseRoomId, "a@sec.com"), null);
        assertThat(chatPresence.onlineEmails(ChatRoomType.PURCHASE, purchaseRoomId)).containsExactly("a@sec.com");
    }

    private Message<byte[]> frame(StompCommand command, String destination, String userEmail) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setDestination(destination);
        accessor.setSessionId("test-session");
        accessor.setSubscriptionId("sub-0");
        accessor.setSessionAttributes(new HashMap<>());
        if (userEmail != null) {
            accessor.setUser(new UsernamePasswordAuthenticationToken(userEmail, null, List.of()));
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private void login(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }
}
