package com.be16_2nd.SmartFridge.chat.service;

import com.be16_2nd.SmartFridge.chat.domain.ChatParticipant;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoom;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.domain.ParticipantRole;
import com.be16_2nd.SmartFridge.chat.dto.ChatMessageDto;
import com.be16_2nd.SmartFridge.chat.dto.ChatRoomCreateDto;
import com.be16_2nd.SmartFridge.chat.dto.MyChatListResDto;
import com.be16_2nd.SmartFridge.chat.repository.ChatRoomRepository;
import com.be16_2nd.SmartFridge.chat.repository.PurchasePostRepository;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeCreateDto;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeMemberWithRoomIdResDto;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ChatRoomStructureTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired FridgeService fridgeService;
    @Autowired FridgeRepository fridgeRepository;
    @Autowired ChatService chatService;
    @Autowired ManagerChatRoomLifecycle lifecycle;
    @Autowired ChatRoomRepository chatRoomRepository;
    @Autowired PurchasePostRepository purchasePostRepository;
    @Autowired EntityManager em;

    Long fridgeId;

    @BeforeEach
    void setUp() {
        memberService.socialLogin(SocialType.GOOGLE, "g-m", "manager@test.com", true, "관리자", null);
        memberService.socialLogin(SocialType.GOOGLE, "g-a", "a@test.com", true, "회원A", null);
        memberService.socialLogin(SocialType.GOOGLE, "g-b", "b@test.com", true, "회원B", null);

        login("manager@test.com");
        fridgeId = fridgeService.create(FridgeCreateDto.builder().fridgeName("테스트냉장고").build()).getFridgeId();
        String inviteCode = fridgeRepository.findById(fridgeId).orElseThrow().getInviteCode();

        login("a@test.com");
        fridgeService.joinFridge(inviteCode);
        login("b@test.com");
        fridgeService.joinFridge(inviteCode);
        flushClear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 일대일_채팅방은_방주인과_관리자가_참여자로_저장된다() {
        ChatRoom room = managerRoomOf("a@test.com");

        assertThat(roles(room)).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a@test.com", ParticipantRole.MEMBER,
                "manager@test.com", ParticipantRole.MANAGER));
        // 관리자 본인 방에는 본인만 있다
        assertThat(roles(managerRoomOf("manager@test.com"))).containsExactlyEntriesOf(Map.of(
                "manager@test.com", ParticipantRole.MEMBER));
    }

    @Test
    void 안읽은_메시지_수는_읽음_커서로_계산되고_읽음처리하면_0이_된다() {
        Long roomId = managerRoomOf("a@test.com").getId();
        send(roomId, ChatRoomType.MANAGER, "a@test.com", "안녕하세요");
        send(roomId, ChatRoomType.MANAGER, "a@test.com", "질문 있어요");
        flushClear();

        login("manager@test.com");
        assertThat(managerUnreadOf("a@test.com")).isEqualTo(2L);
        // 보낸 사람은 안 읽은 메시지가 없다
        login("a@test.com");
        assertThat(myManagerRoomUnread()).isZero();

        login("manager@test.com");
        chatService.messageRead(roomId, ChatRoomType.MANAGER);
        flushClear();
        assertThat(managerUnreadOf("a@test.com")).isZero();
    }

    @Test
    void 관리자_위임시_일대일방_관리자가_교체되고_읽음_커서를_이어받는다() {
        Long roomId = managerRoomOf("a@test.com").getId();
        send(roomId, ChatRoomType.MANAGER, "a@test.com", "읽은 메시지");
        login("manager@test.com");
        chatService.messageRead(roomId, ChatRoomType.MANAGER);
        send(roomId, ChatRoomType.MANAGER, "a@test.com", "안 읽은 메시지");
        flushClear();

        login("manager@test.com");
        fridgeService.delegateManager(fridgeId, "b@test.com");
        flushClear();

        assertThat(roles(managerRoomOf("a@test.com"))).containsExactlyInAnyOrderEntriesOf(Map.of(
                "a@test.com", ParticipantRole.MEMBER,
                "b@test.com", ParticipantRole.MANAGER));
        // 이전 관리자 방에는 새 관리자가 상대로 들어간다
        assertThat(roles(managerRoomOf("manager@test.com"))).containsExactlyInAnyOrderEntriesOf(Map.of(
                "manager@test.com", ParticipantRole.MEMBER,
                "b@test.com", ParticipantRole.MANAGER));
        // 새 관리자 본인 방에는 관리자 상대가 없다
        assertThat(roles(managerRoomOf("b@test.com"))).containsExactlyEntriesOf(Map.of(
                "b@test.com", ParticipantRole.MEMBER));

        login("b@test.com");
        assertThat(managerUnreadOf("a@test.com")).isEqualTo(1L);
    }

    @Test
    void 공동구매_채팅방은_게시글과_분리되고_방장이_나가면_함께_삭제된다() {
        login("a@test.com");
        Long roomId = chatService.createPurchaseChatRoom(ChatRoomCreateDto.builder()
                .fridgeId(fridgeId).title("우유 공구").maxParticipants(2).build());
        login("b@test.com");
        chatService.addParticipantToPurchaseChat(roomId);
        send(roomId, ChatRoomType.PURCHASE, "a@test.com", "언제 살까요");
        flushClear();

        assertThat(purchasePostRepository.findByChatRoomId(roomId).orElseThrow().getCurrentParticipants()).isEqualTo(2);
        login("b@test.com");
        MyChatListResDto purchaseRoom = chatService.getMyChatRooms(fridgeId).stream()
                .filter(r -> r.getRoomId().equals(roomId)).findFirst().orElseThrow();
        assertThat(purchaseRoom.getUnReadCount()).isEqualTo(1L);
        assertThat(purchaseRoom.getRoomName()).isEqualTo("우유 공구");

        chatService.leavePurchaseChatRoom(roomId);
        flushClear();
        assertThat(purchasePostRepository.findByChatRoomId(roomId).orElseThrow().getCurrentParticipants()).isEqualTo(1);

        login("a@test.com");
        chatService.leavePurchaseChatRoom(roomId);
        flushClear();
        assertThat(chatRoomRepository.findById(roomId)).isEmpty();
        assertThat(purchasePostRepository.findByChatRoomId(roomId)).isEmpty();
    }

    private void send(Long roomId, ChatRoomType type, String senderEmail, String message) {
        chatService.saveMessage(roomId, type, senderEmail, ChatMessageDto.builder()
                .message(message)
                .imageUrls(List.of())
                .build());
    }

    private ChatRoom managerRoomOf(String email) {
        Fridge fridge = fridgeRepository.findById(fridgeId).orElseThrow();
        Member member = memberRepository.findByEmail(email).orElseThrow();
        return lifecycle.findManagerChatRoom(fridge, member).orElseThrow();
    }

    private Map<String, ParticipantRole> roles(ChatRoom room) {
        return room.getParticipants().stream()
                .collect(Collectors.toMap(p -> p.getMember().getEmail(), ChatParticipant::getRole));
    }

    private Long managerUnreadOf(String memberEmail) {
        return fridgeService.findByFridgeMember(fridgeId).stream()
                .filter(m -> m.getMemberEmail().equals(memberEmail))
                .map(FridgeMemberWithRoomIdResDto::getUnReadCount)
                .findFirst().orElseThrow();
    }

    private Long myManagerRoomUnread() {
        return chatService.getMyChatRooms(fridgeId).get(0).getUnReadCount();
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
