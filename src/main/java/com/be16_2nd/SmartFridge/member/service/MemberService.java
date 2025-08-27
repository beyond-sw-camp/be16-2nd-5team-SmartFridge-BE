package com.be16_2nd.SmartFridge.member.service;

import com.be16_2nd.SmartFridge.email.service.EmailService;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.domain.ResetToken;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import com.be16_2nd.SmartFridge.member.dto.*;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MemberService {


    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    @Qualifier("emailRedisTemplate")
    private final RedisTemplate<String, Object> emailRedisTemplate;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, EmailService emailService, RedisTemplate<String, Object> emailRedisTemplate) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.emailRedisTemplate = emailRedisTemplate;
    }

    public Member save(MemberCreateDto memberCreateDto){
        if (memberRepository.findByEmail(memberCreateDto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다.");
        }
        String newPassword = memberCreateDto.getPassword();
        String newPasswordConfirm = memberCreateDto.getPasswordConfirm();

        if (!newPassword.equals(newPasswordConfirm)){
            throw new IllegalArgumentException("비밀번호를 확인해 주세요");
        }
        Member member = memberRepository.save(memberCreateDto.toEntity(passwordEncoder.encode(memberCreateDto.getPassword())));
        return member;
    }

    public Member doLogin(LoginReqDto loginReqDto){
        Optional<Member> optionalMember = memberRepository.findByEmail(loginReqDto.getEmail());
        boolean check = true;
        if (!optionalMember.isPresent()){
            check = false;
        } else {
            if (!passwordEncoder.matches(loginReqDto.getPassword(), optionalMember.get().getPassword())){
                check = false;
            }
        }
        if (!check){
            throw new IllegalArgumentException("id, email또는 비밀번호가 일치하지 않습니다.");
        }
        return optionalMember.get();
    }
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElse(null);
    }

    public Member getMemberBySocialId(String socialId){
        Member member = memberRepository.findBySocialId(socialId).orElse(null);
        return member;
    }

    public Member createOauth(String socialId, String email, String name, String picture, SocialType socialType){
        Member member = Member.builder()
                .email(email)
                .name(name)
                .profileImage(picture)
                .socialType(socialType)
                .socialId(socialId)
                .build();
        memberRepository.save(member);
        return member;
    }

    @Transactional(readOnly = true)
    public List<MemberResDto> findAll(){
        return memberRepository.findAll().stream()
                .map(m->MemberResDto.fromEntity(m)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemberResDto myInfo(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        return MemberResDto.fromEntity(member);
    }

    public void delete(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("존재하지 않는 회원입니다."));
        memberRepository.delete(member);
    }

    public Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
    }

//    public boolean sendResetToken(ForgotPasswordReqDto forgotPasswordReqDto) throws MessagingException {
//        Member member = memberRepository.findByEmail(forgotPasswordReqDto.getEmail())
//                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 계정입니다."));
//
//        String token = UUID.randomUUID().toString();
//        ResetToken resetToken = new ResetToken(token, forgotPasswordReqDto.getEmail());
//        String redisKey = "password_reset:" + forgotPasswordReqDto.getEmail();
//        emailRedisTemplate.opsForValue().set(redisKey, resetToken, Duration.ofMinutes(30));
//
//        // 4. 이메일 발송
//        emailService.sendEmailForUpdatePw(forgotPasswordReqDto.getEmail(), resetToken);
//    }
//
//        return true;
//    }
//
//    public boolean resetPassword(ResetPasswordReqDto resetPasswordReqDto) {
//        ResetToken resetToken = (ResetToken) emailRedisTemplate.opsForValue()
//                .get("reset_token:" + resetPasswordReqDto.getToken());
//
//        if (resetToken == null) {
//            return false;
//        }
//
//        Optional<Member> optMember = memberRepository.findByEmail(resetToken.getEmail());
//        if (optMember.isEmpty()) {
//            emailRedisTemplate.delete("reset_token:" + resetPasswordReqDto.getToken());
//            return false;
//        }
//
//        Member member = optMember.get();
//        member.setPassword(passwordEncoder.encode(resetPasswordReqDto.getNewPassword()));
//        memberRepository.save(member);
//
//        emailRedisTemplate.delete("reset_token:" + resetPasswordReqDto.getToken());
//
//        return true;
//    }
}
