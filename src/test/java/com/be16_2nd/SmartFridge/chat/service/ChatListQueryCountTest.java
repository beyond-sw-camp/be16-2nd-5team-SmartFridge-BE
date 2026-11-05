package com.be16_2nd.SmartFridge.chat.service;

import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.dto.ChatMessageDto;
import com.be16_2nd.SmartFridge.chat.dto.ChatRoomCreateDto;
import com.be16_2nd.SmartFridge.chat.dto.MyChatListResDto;
import com.be16_2nd.SmartFridge.chat.dto.PurchaseChatRoomListResDto;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeCreateDto;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeMemberWithRoomIdResDto;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeRepository;
import com.be16_2nd.SmartFridge.fridge.service.FridgeService;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import com.be16_2nd.SmartFridge.member.service.MemberService;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * 채팅 목록 조회의 쿼리 수가 방·멤버 수와 상관없이 일정한지 확인 (N+1 회귀 방지)
 */
@SpringBootTest
@Transactional
class ChatListQueryCountTest {

    @Autowired MemberService memberService;
    @Autowired FridgeService fridgeService;
    @Autowired FridgeRepository fridgeRepository;
    @Autowired ChatService chatService;
    @Autowired EntityManager em;

    Statistics statistics;
    Long fridgeId;
    String inviteCode;
    int memberSeq;

    @BeforeEach
    void setUp() {
        statistics = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);

        memberService.socialLogin(SocialType.GOOGLE, "q-m", "manager@q.com", true, "관리자", null);
        memberService.socialLogin(SocialType.GOOGLE, "q-me", "me@q.com", true, "나", null);
        login("manager@q.com");
        fridgeId = fridgeService.create(FridgeCreateDto.builder().fridgeName("쿼리냉장고").build()).getFridgeId();
        inviteCode = fridgeRepository.findById(fridgeId).orElseThrow().getInviteCode();
        login("me@q.com");
        fridgeService.joinFridge(inviteCode);
    }

    @AfterEach
    void tearDown() {
        statistics.setStatisticsEnabled(false);
        SecurityContextHolder.clearContext();
    }

    @Test
    void 내_채팅방_목록과_모집중_목록은_방이_늘어도_쿼리_수가_같다() {
        addRoomsJoinedByMe(2);
        login("me@q.com");
        long myRoomsSmall = countQueries(() -> chatService.getMyChatRooms(fridgeId));
        long openRoomsSmall = countQueries(() -> chatService.getPurchaseChatRooms(fridgeId));

        addRoomsJoinedByMe(5);
        login("me@q.com");
        long myRoomsLarge = countQueries(() -> chatService.getMyChatRooms(fridgeId));
        long openRoomsLarge = countQueries(() -> chatService.getPurchaseChatRooms(fridgeId));

        assertThat(myRoomsLarge).isEqualTo(myRoomsSmall);
        assertThat(openRoomsLarge).isEqualTo(openRoomsSmall);
    }

    @Test
    void 목록의_참여자_수와_안읽은_수가_정확하다() {
        // 방마다 개설자 + 나 = 2명. 참여 전 메시지는 읽음 처리되므로, 참여 후 보낸 메시지만 안 읽은 수에 잡힌다
        List<Long> roomIds = addRoomsJoinedByMe(2);
        String creator = "member" + (memberSeq - 1) + "@q.com";
        chatService.saveMessage(roomIds.get(1), ChatRoomType.PURCHASE, creator, ChatMessageDto.builder().message("하나 더").build());
        flushClear();

        login("me@q.com");
        Map<Long, MyChatListResDto> mine = chatService.getMyChatRooms(fridgeId).stream()
                .filter(r -> roomIds.contains(r.getRoomId()))
                .collect(Collectors.toMap(MyChatListResDto::getRoomId, r -> r));
        assertThat(mine.get(roomIds.get(0)).getCurrentParticipants()).isEqualTo(2);
        assertThat(mine.get(roomIds.get(0)).getUnReadCount()).isZero();
        assertThat(mine.get(roomIds.get(1)).getUnReadCount()).isEqualTo(1L);

        Map<Long, PurchaseChatRoomListResDto> open = chatService.getPurchaseChatRooms(fridgeId).stream()
                .collect(Collectors.toMap(PurchaseChatRoomListResDto::getRoomId, r -> r));
        assertThat(open.get(roomIds.get(0)).getCurrentParticipants()).isEqualTo(2);
    }

    @Test
    void 관리자의_멤버_목록은_멤버가_늘어도_쿼리_수가_같고_안읽은_수가_정확하다() {
        addMembers(2);
        login("manager@q.com");
        long small = countQueries(() -> fridgeService.findByFridgeMember(fridgeId));

        addMembers(5);
        login("manager@q.com");
        long large = countQueries(() -> fridgeService.findByFridgeMember(fridgeId));
        assertThat(large).isEqualTo(small);

        // me 가 관리자에게 2개 보냄 → 관리자 기준 me 의 방만 2
        Long myRoomId = fridgeService.findByFridgeMember(fridgeId).stream()
                .filter(m -> m.getMemberEmail().equals("me@q.com")).findFirst().orElseThrow().getRoomId();
        chatService.saveMessage(myRoomId, ChatRoomType.MANAGER, "me@q.com", ChatMessageDto.builder().message("1").build());
        chatService.saveMessage(myRoomId, ChatRoomType.MANAGER, "me@q.com", ChatMessageDto.builder().message("2").build());
        flushClear();

        login("manager@q.com");
        Map<String, Long> unread = fridgeService.findByFridgeMember(fridgeId).stream()
                .collect(Collectors.toMap(FridgeMemberWithRoomIdResDto::getMemberEmail, FridgeMemberWithRoomIdResDto::getUnReadCount));
        assertThat(unread.get("me@q.com")).isEqualTo(2L);
        assertThat(unread.values().stream().mapToLong(Long::longValue).sum()).isEqualTo(2L);
    }

    // 새 회원이 방을 만들고 내가 참여, 개설자가 메시지 1개
    private List<Long> addRoomsJoinedByMe(int count) {
        List<Long> roomIds = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            String creator = addMembers(1).get(0);
            login(creator);
            Long roomId = chatService.createPurchaseChatRoom(ChatRoomCreateDto.builder()
                    .fridgeId(fridgeId).title("공구" + roomId(i)).maxParticipants(5).build());
            chatService.saveMessage(roomId, ChatRoomType.PURCHASE, creator, ChatMessageDto.builder().message("안녕").build());
            login("me@q.com");
            chatService.addParticipantToPurchaseChat(roomId);
            roomIds.add(roomId);
        }
        flushClear();
        return roomIds;
    }

    private String roomId(int i) {
        return memberSeq + "-" + i;
    }

    private List<String> addMembers(int count) {
        List<String> emails = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            String email = "member" + memberSeq + "@q.com";
            memberService.socialLogin(SocialType.GOOGLE, "q-" + memberSeq, email, true, "회원" + memberSeq, null);
            memberSeq++;
            login(email);
            fridgeService.joinFridge(inviteCode);
            emails.add(email);
        }
        flushClear();
        return emails;
    }

    private long countQueries(Supplier<?> query) {
        flushClear();
        statistics.clear();
        query.get();
        return statistics.getPrepareStatementCount();
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
