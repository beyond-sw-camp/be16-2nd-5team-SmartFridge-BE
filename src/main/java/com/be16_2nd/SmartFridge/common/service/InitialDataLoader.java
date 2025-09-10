package com.be16_2nd.SmartFridge.common.service;

import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.domain.Role;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    @Value("${app.admin.name}")
    private String adminName;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) throws Exception {
        if(memberRepository.findByEmail(adminEmail).isPresent()){
            return;
        }
        Member member = Member.builder()
                .name(adminName)
                .email(adminEmail)
                .role(Role.ADMIN)
                .password(passwordEncoder.encode(adminPassword))
                .build();
        memberRepository.save(member);
    }
}
