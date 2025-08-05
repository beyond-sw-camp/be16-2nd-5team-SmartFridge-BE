package com.be16_2nd.SmartFridge.fridge.service;

import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.domain.FridgeMember;
import com.be16_2nd.SmartFridge.fridge.domain.Type;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeCreateDto;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeCreateResDto;
import com.be16_2nd.SmartFridge.fridge.dto.FridgeMemberResDto;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FridgeService {

    private final FridgeRepository fridgeRepository;
    private final MemberRepository memberRepository;
    private final FridgeMemberRepository fridgeMemberRepository;


    public FridgeCreateResDto create(FridgeCreateDto fridgeCreateDto){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        if (fridgeRepository.findByFridgeName(fridgeCreateDto.getFridgeName()).isPresent()){
            throw new IllegalArgumentException("이미 동일한 이름의 냉장고가 있습니다.");
        }
        Fridge fridge = fridgeCreateDto.toEntity();
        fridge.getFridgeMemberList().add(FridgeMember.builder()
                        .fridge(fridge)
                        .member(member)
                        .type(Type.MANAGER)
                        .build());
        fridge.setInviteCode(UUID.randomUUID().toString());
        String inviteLink = "https://smart_fridge.com/fridge/join?code=" + fridge.getInviteCode();
        fridgeRepository.save(fridge);
        FridgeCreateResDto dto = FridgeCreateResDto.builder()
                .fridgeId(fridge.getFridgeId())
                .inviteLink(inviteLink)
                .build();

        return dto;
    }

    public List<FridgeMemberResDto> findByFridgeId(Long fridgeId){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new EntityNotFoundException("member is not found"));

        Fridge fridge = fridgeRepository.findById(fridgeId)
                .orElseThrow(()->new EntityNotFoundException("fridge is not found"));

        return fridge.getFridgeMemberList().stream()
                .map(fridgeMember -> FridgeMemberResDto.fromEntity(fridgeMember))
                .collect(Collectors.toList());
    }

    public Long joinFridge(String inviteCode){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        Fridge fridge = fridgeRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 초대 코드입니다."));

        if (fridgeMemberRepository.existsByFridgeAndMember(fridge, member)) {
            throw new IllegalArgumentException("이미 가입된 냉장고입니다.");
        }

        fridgeMemberRepository.save(FridgeMember.builder()
                .fridge(fridge)
                .member(member)
                .build());
        return fridge.getFridgeId();
    }
}
