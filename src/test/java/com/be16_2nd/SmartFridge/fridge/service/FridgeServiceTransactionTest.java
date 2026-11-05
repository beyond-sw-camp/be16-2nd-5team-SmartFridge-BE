package com.be16_2nd.SmartFridge.fridge.service;

import com.be16_2nd.SmartFridge.chat.service.ManagerChatRoomLifecycle;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.domain.Type;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeCreateDto;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeListDto;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import com.be16_2nd.SmartFridge.member.service.MemberService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 * 서비스 메서드가 스스로 트랜잭션을 여는지 확인한다.
 * 테스트에 @Transactional 을 붙이면 바깥 트랜잭션이 문제를 가리므로 붙이지 않는다 (운영의 open-in-view 와 같은 효과).
 * 데이터는 커밋되지만 로컬 DB 는 ddl-auto: create 라 다음 기동 때 초기화되고, 이메일은 실행마다 다르다.
 */
@SpringBootTest
class FridgeServiceTransactionTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired FridgeService fridgeService;
    @Autowired FridgeRepository fridgeRepository;
    @Autowired FridgeMemberRepository fridgeMemberRepository;
    @Autowired ManagerChatRoomLifecycle managerChatRoomLifecycle;
    @Autowired TransactionTemplate transactionTemplate;

    final String run = Long.toString(System.nanoTime());
    String manager;
    String joiner;

    @BeforeEach
    void setUp() {
        manager = "manager-" + run + "@tx.com";
        joiner = "joiner-" + run + "@tx.com";
        memberService.socialLogin(SocialType.GOOGLE, "m-" + run, manager, true, "관리자", null);
        memberService.socialLogin(SocialType.GOOGLE, "j-" + run, joiner, true, "참여자", null);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 트랜잭션_밖에서_불러도_생성_참여_조회가_지연_로딩_오류_없이_동작한다() {
        login(manager);
        Long fridgeId = fridgeService.create(FridgeCreateDto.builder().fridgeName("트랜잭션냉장고").build()).getFridgeId();
        String inviteCode = fridgeRepository.findById(fridgeId).orElseThrow().getInviteCode();

        login(joiner);
        fridgeService.joinFridge(inviteCode);
        List<FridgeListDto> myFridges = fridgeService.findMyFridges();
        assertThat(myFridges).extracting(FridgeListDto::getFridgeName).containsExactly("트랜잭션냉장고");
        assertThat(fridgeService.fridgeDetail(fridgeId).getFridgeName()).isEqualTo("트랜잭션냉장고");

        login(manager);
        assertThat(fridgeService.findByFridgeMember(fridgeId)).hasSize(2);
    }

    @Test
    void 냉장고_참여_중간에_실패하면_멤버_추가와_채팅방_생성이_모두_취소된다() {
        login(manager);
        Long fridgeId = fridgeService.create(FridgeCreateDto.builder().fridgeName("실패냉장고").build()).getFridgeId();
        String inviteCode = fridgeRepository.findById(fridgeId).orElseThrow().getInviteCode();

        // 관리자 알림 단계에서 실패하도록 관리자 멤버를 없앤다 (멤버 저장·채팅방 생성 뒤에 실패)
        transactionTemplate.executeWithoutResult(s -> {
            Fridge fridge = fridgeRepository.findById(fridgeId).orElseThrow();
            fridgeMemberRepository.delete(fridgeMemberRepository.findByFridgeAndType(fridge, Type.MANAGER).orElseThrow());
        });

        login(joiner);
        assertThatThrownBy(() -> fridgeService.joinFridge(inviteCode)).isInstanceOf(EntityNotFoundException.class);

        transactionTemplate.executeWithoutResult(s -> {
            Fridge fridge = fridgeRepository.findById(fridgeId).orElseThrow();
            Member member = memberRepository.findByEmail(joiner).orElseThrow();
            assertThat(fridgeMemberRepository.existsByFridgeAndMember(fridge, member)).isFalse();
            assertThat(managerChatRoomLifecycle.findManagerChatRoom(fridge, member)).isEmpty();
        });
    }

    private void login(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }
}
