package com.be16_2nd.SmartFridge.member.service;

import com.be16_2nd.SmartFridge.common.service.RedisUtil;
import com.be16_2nd.SmartFridge.email.dto.EmailDto;
import com.be16_2nd.SmartFridge.email.service.EmailService;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import com.be16_2nd.SmartFridge.member.dto.*;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MemberService {


    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisUtil redisUtil;


    public Member save(MemberCreateDto memberCreateDto){
        if (memberRepository.findByEmail(memberCreateDto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다.");
        }
        String password = memberCreateDto.getPassword();
        String passwordConfirm = memberCreateDto.getPasswordConfirm();

        if (!password.equals(passwordConfirm)){
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

    public Member updatePw(UpdatePwDto updatePwDto){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new EntityNotFoundException("가입되지 않은 이메일입니다."));
        String password = updatePwDto.getNewPassword();
        String passwordConfirm = updatePwDto.getNewPasswordConfirm();

        if (!password.equals(passwordConfirm)){
            throw new IllegalArgumentException("비밀번호를 확인해 주세요");
        }

        member.updatePw(passwordEncoder.encode(password));
        memberRepository.save(member);
        return member;
    }
}
