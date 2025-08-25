package com.be16_2nd.SmartFridge.member.service;

import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import com.be16_2nd.SmartFridge.member.dto.LoginReqDto;
import com.be16_2nd.SmartFridge.member.dto.MemberCreateDto;
import com.be16_2nd.SmartFridge.member.dto.MemberResDto;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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
        member.delete(member);
    }

    public Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
    }
}
