package com.be16_2nd.SmartFridge.member.service;

import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import com.be16_2nd.SmartFridge.member.dto.LoginReqDto;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class MemberServiceLoginTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired EntityManager em;

    @Value("${app.admin.email}") String adminEmail;
    @Value("${app.admin.password}") String adminPassword;

    @Test
    void 처음_소셜로그인하면_회원이_생성되고_재로그인시_같은_회원() {
        Member first = memberService.socialLogin(SocialType.GOOGLE, "g-1", "new@test.com", true, "새회원", null);
        flushClear();
        Member again = memberService.socialLogin(SocialType.GOOGLE, "g-1", "new@test.com", true, "새회원", null);

        assertThat(again.getId()).isEqualTo(first.getId());
        assertThat(memberRepository.findByEmail("new@test.com")).isPresent();
    }

    @Test
    void 같은_이메일의_다른_소셜계정은_연결하지_않고_기존_제공자로_안내() {
        Member google = memberService.socialLogin(SocialType.GOOGLE, "g-2", "same@test.com", true, "회원", null);
        flushClear();

        assertThatThrownBy(() ->
                memberService.socialLogin(SocialType.KAKAO, "k-2", "same@test.com", true, "회원", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("구글");
        flushClear();

        assertThat(memberRepository.findById(google.getId()).orElseThrow().getSocialType()).isEqualTo(SocialType.GOOGLE);
        assertThat(memberService.socialLogin(SocialType.GOOGLE, "g-2", "same@test.com", true, "회원", null).getId())
                .isEqualTo(google.getId());
    }

    @Test
    void 인증되지_않은_이메일로는_신규_가입_불가() {
        assertThatThrownBy(() ->
                memberService.socialLogin(SocialType.KAKAO, "k-3", "victim@test.com", false, "공격자", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(memberRepository.findByEmail("victim@test.com")).isEmpty();
    }

    @Test
    void 이메일이_없으면_로그인_거부() {
        assertThatThrownBy(() ->
                memberService.socialLogin(SocialType.KAKAO, "k-4", null, false, "회원", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 관리자_이메일로는_소셜계정_연결_불가() {
        assertThatThrownBy(() ->
                memberService.socialLogin(SocialType.NAVER, "n-5", adminEmail, true, "관리자흉내", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 관리자는_일반_로그인_가능() {
        Member admin = memberService.doAdminLogin(new LoginReqDto(adminEmail, adminPassword));
        assertThat(admin.getEmail()).isEqualTo(adminEmail);
    }

    @Test
    void 일반_회원은_비밀번호가_있어도_일반_로그인_불가() {
        memberRepository.save(Member.builder()
                .email("user@test.com")
                .password(passwordEncoder.encode("pw1234"))
                .build());
        flushClear();

        assertThatThrownBy(() -> memberService.doAdminLogin(new LoginReqDto("user@test.com", "pw1234")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void flushClear() {
        em.flush();
        em.clear();
    }
}
