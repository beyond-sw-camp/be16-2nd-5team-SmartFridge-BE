package com.be16_2nd.SmartFridge.chat.service;

import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.dto.ChatMessageDto;
import com.be16_2nd.SmartFridge.chat.dto.ChatRoomCreateDto;
import com.be16_2nd.SmartFridge.chat.repository.ChatRoomRepository;
import com.be16_2nd.SmartFridge.chat.repository.PurchasePostRepository;
import com.be16_2nd.SmartFridge.chat.service.Validator.ChatRoomParticipantValidator;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeCreateDto;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeRepository;
import com.be16_2nd.SmartFridge.fridge.service.FridgeService;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import com.be16_2nd.SmartFridge.member.service.MemberService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ChatFridgeLeaveTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired FridgeService fridgeService;
    @Autowired FridgeRepository fridgeRepository;
    @Autowired FridgeMemberRepository fridgeMemberRepository;
    @Autowired ChatService chatService;
    @Autowired ChatRoomRepository chatRoomRepository;
    @Autowired PurchasePostRepository purchasePostRepository;
    @Autowired ChatRoomParticipantValidator participantValidator;
    @Autowired EntityManager em;

    Long fridgeId;
    Long roomByA;
    Long roomByB;

    @BeforeEach
    void setUp() {
        memberService.socialLogin(SocialType.GOOGLE, "l-m", "manager@l.com", true, "관리자", null);
        memberService.socialLogin(SocialType.GOOGLE, "l-a", "a@l.com", true, "회원A", null);
        memberService.socialLogin(SocialType.GOOGLE, "l-b", "b@l.com", true, "회원B", null);

        login("manager@l.com");
        fridgeId = fridgeService.create(FridgeCreateDto.builder().fridgeName("나가기냉장고").build()).getFridgeId();
        String inviteCode = fridgeRepository.findById(fridgeId).orElseThrow().getInviteCode();
        login("a@l.com");
        fridgeService.joinFridge(inviteCode);
        login("b@l.com");
        fridgeService.joinFridge(inviteCode);

        // A가 만든 방에 B 참여, B가 만든 방에 A 참여
        login("a@l.com");
        roomByA = createRoom("A의 공구");
        login("b@l.com");
        roomByB = createRoom("B의 공구");
        chatService.addParticipantToPurchaseChat(roomByA);
        login("a@l.com");
        chatService.addParticipantToPurchaseChat(roomByB);
        flushClear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 냉장고를_스스로_나가면_만든_방은_삭제되고_참여한_방에서는_빠진다() {
        login("a@l.com");
        fridgeService.leaveFridge(fridgeId);
        flushClear();

        assertThat(chatRoomRepository.findById(roomByA)).isEmpty();
        assertThat(purchasePostRepository.findByChatRoomId(roomByA)).isEmpty();
        assertThat(participantEmails(roomByB)).containsExactly("b@l.com");
    }

    @Test
    void 강퇴해도_같은_규칙으로_정리된다() {
        login("manager@l.com");
        fridgeService.removeMember(fridgeId, "a@l.com");
        flushClear();

        assertThat(chatRoomRepository.findById(roomByA)).isEmpty();
        assertThat(participantEmails(roomByB)).containsExactly("b@l.com");
    }

    @Test
    void 참여자로_남아_있어도_냉장고를_떠난_회원은_채팅방에_접근할_수_없다() {
        // 예전 버전에서 남은 데이터: 냉장고 멤버만 지워지고 채팅 참여자는 남은 상태
        Fridge fridge = fridgeRepository.findById(fridgeId).orElseThrow();
        Member a = memberRepository.findByEmail("a@l.com").orElseThrow();
        fridgeMemberRepository.deleteByFridgeAndMember(fridge, a);
        flushClear();

        assertThatThrownBy(() -> participantValidator.validateParticipant("a@l.com", roomByB, ChatRoomType.PURCHASE))
                .isInstanceOf(AuthorizationServiceException.class);
        assertThatThrownBy(() -> chatService.saveMessage(roomByB, ChatRoomType.PURCHASE, "a@l.com",
                ChatMessageDto.builder().message("나간 뒤 메시지").build()))
                .isInstanceOf(AuthorizationServiceException.class);
    }

    private Long createRoom(String title) {
        return chatService.createPurchaseChatRoom(ChatRoomCreateDto.builder()
                .fridgeId(fridgeId).title(title).maxParticipants(3).build());
    }

    private List<String> participantEmails(Long roomId) {
        return chatRoomRepository.findById(roomId).orElseThrow().getParticipants().stream()
                .map(p -> p.getMember().getEmail()).toList();
    }

    private void login(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }

    private void flushClear() {
        em.flush();
        em.clear();
    }
}
