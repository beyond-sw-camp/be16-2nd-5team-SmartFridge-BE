package com.be16_2nd.SmartFridge.chat.service;

import com.be16_2nd.SmartFridge.chat.dto.ChatRoomCreateDto;
import com.be16_2nd.SmartFridge.chat.repository.PurchasePostRepository;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeCreateDto;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeRepository;
import com.be16_2nd.SmartFridge.fridge.service.FridgeService;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import com.be16_2nd.SmartFridge.member.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * 실제 트랜잭션을 동시에 실행해야 하므로 @Transactional 을 붙이지 않는다 (데이터가 커밋됨)
 * 로컬 DB 는 ddl-auto: create 라 다음 기동 때 초기화되고, 이메일은 실행마다 달라 충돌하지 않는다
 */
@SpringBootTest
class ChatPurchaseJoinConcurrencyTest {

    static final int JOINERS = 6;
    static final int MAX_PARTICIPANTS = 3; // 개설자 포함

    @Autowired MemberService memberService;
    @Autowired FridgeService fridgeService;
    @Autowired FridgeRepository fridgeRepository;
    @Autowired ChatService chatService;
    @Autowired PurchasePostRepository purchasePostRepository;
    @Autowired TransactionTemplate transactionTemplate;

    final String run = Long.toString(System.nanoTime());

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 동시에_참여해도_정원을_넘지_않는다() throws Exception {
        String creator = email("creator");
        memberService.socialLogin(SocialType.GOOGLE, "c-" + run, creator, true, "개설자", null);
        List<String> joiners = new ArrayList<>();
        for (int i = 0; i < JOINERS; i++) {
            joiners.add(email("j" + i));
            memberService.socialLogin(SocialType.GOOGLE, "j" + i + "-" + run, joiners.get(i), true, "참여자" + i, null);
        }

        login(creator);
        Long fridgeId = fridgeService.create(FridgeCreateDto.builder().fridgeName("동시성냉장고").build()).getFridgeId();
        String inviteCode = fridgeRepository.findById(fridgeId).orElseThrow().getInviteCode();
        for (String joiner : joiners) {
            login(joiner);
            fridgeService.joinFridge(inviteCode);
        }
        login(creator);
        Long roomId = chatService.createPurchaseChatRoom(ChatRoomCreateDto.builder()
                .fridgeId(fridgeId).title("동시 참여").maxParticipants(MAX_PARTICIPANTS).build());

        // 모든 스레드가 준비된 뒤 한꺼번에 참여
        ExecutorService pool = Executors.newFixedThreadPool(JOINERS);
        CountDownLatch ready = new CountDownLatch(JOINERS);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger rejected = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();
        for (String joiner : joiners) {
            futures.add(pool.submit(() -> {
                login(joiner);
                ready.countDown();
                start.await();
                try {
                    chatService.addParticipantToPurchaseChat(roomId);
                } catch (IllegalArgumentException e) {
                    rejected.incrementAndGet();
                } finally {
                    SecurityContextHolder.clearContext();
                }
                return null;
            }));
        }
        ready.await(10, TimeUnit.SECONDS);
        start.countDown();
        for (Future<?> f : futures) {
            f.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();

        Integer participants = transactionTemplate.execute(s ->
                purchasePostRepository.findByChatRoomId(roomId).orElseThrow().getCurrentParticipants());
        assertThat(participants).isEqualTo(MAX_PARTICIPANTS);
        assertThat(rejected.get()).isEqualTo(JOINERS - (MAX_PARTICIPANTS - 1));
    }

    private String email(String name) {
        return name + "-" + run + "@concurrency.com";
    }

    private void login(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }
}
