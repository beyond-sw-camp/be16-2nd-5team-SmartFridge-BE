package com.be16_2nd.SmartFridge.common.service;

import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.domain.FridgeMember;
import com.be16_2nd.SmartFridge.fridge.domain.Type;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FridgeAccessValidator {
    private final MemberRepository memberRepository;
    private final FridgeRepository fridgeRepository;
    private final FridgeMemberRepository fridgeMemberRepository;

    public FridgeContext validate(Long fridgeId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));

        Fridge fridge = fridgeRepository.findById(fridgeId)
                .orElseThrow(() -> new EntityNotFoundException("없는 냉장고입니다."));

        FridgeMember fridgeMember = fridgeMemberRepository.findByFridgeAndMember(fridge, member)
                .orElseThrow(() -> new AccessDeniedException("이 냉장고에 대한 접근 권한이 없습니다."));

        return new FridgeContext(fridge, member, fridgeMember.getType());
    }

    public record FridgeContext(Fridge fridge, Member member, Type type) {}
}
