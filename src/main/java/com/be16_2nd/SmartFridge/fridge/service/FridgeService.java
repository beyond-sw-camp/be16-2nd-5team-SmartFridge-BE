package com.be16_2nd.SmartFridge.fridge.service;

import com.be16_2nd.SmartFridge.Post.repository.PostRepository;
import com.be16_2nd.SmartFridge.chat.repository.ManagerChatRoomRepository;
import com.be16_2nd.SmartFridge.common.service.FridgeAccessValidator;
import com.be16_2nd.SmartFridge.chat.service.ManagerChatRoomLifecycle;
import com.be16_2nd.SmartFridge.common.service.FridgeAccessValidator;
import com.be16_2nd.SmartFridge.food.domain.Food;
import com.be16_2nd.SmartFridge.food.dto.FoodResDto;
import com.be16_2nd.SmartFridge.food.repository.FoodRepository;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.domain.FridgeMember;
import com.be16_2nd.SmartFridge.fridge.domain.Type;
import com.be16_2nd.SmartFridge.fridge.dto.*;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import com.be16_2nd.SmartFridge.notification.domain.NotificationType;
import com.be16_2nd.SmartFridge.notification.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FridgeService {

    private final FridgeRepository fridgeRepository;
    private final MemberRepository memberRepository;
    private final FridgeMemberRepository fridgeMemberRepository;
    private final ManagerChatRoomRepository managerChatRoomRepository;
    private final ManagerChatRoomLifecycle managerChatRoomLifecycle;
    private final NotificationService notificationService;

    private final FridgeAccessValidator fridgeAccessValidator;
    private final FoodRepository foodRepository;
    private final PostRepository postRepository;

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
        String inviteLink = fridge.getId() + fridge.getInviteCode();
        fridgeRepository.save(fridge);
        FridgeCreateResDto dto = FridgeCreateResDto.builder()
                .fridgeId(fridge.getId())
                .inviteLink(inviteLink)
                .build();
        managerChatRoomLifecycle.createManagerChatRoom(member, fridge);
        return dto;
    }

    @Transactional
    public Long update(Long fridgeId, FridgeUpdateDto fridgeUpdateDto){
        FridgeAccessValidator.FridgeContext context = fridgeAccessValidator.validate(fridgeId);
        Fridge fridge = context.fridge();
        if(!context.type().equals(Type.MANAGER)){
            throw new AccessDeniedException("MANAGER만 수정 할 수 있습니다");
        }

        String newName = fridgeUpdateDto.getFridgeName();
        if (newName != null && !newName.equals(fridge.getFridgeName())) {
            Optional<Fridge> existing = fridgeRepository.findByFridgeName(newName);
            if (existing.isPresent() && !existing.get().getId().equals(fridgeId)) {
                throw new IllegalArgumentException("이미 동일한 이름의 냉장고가 있습니다.");
            }
        }
        fridge.updateFridge(fridgeUpdateDto);
        return fridge.getId();
    }

    public List<FridgeMemberResDto> findByFridgeMember(Long fridgeId){
        FridgeAccessValidator.FridgeContext context = fridgeAccessValidator.validate(fridgeId);
        Fridge fridge = context.fridge();
        if (context.type() != Type.MANAGER) {
            throw new AccessDeniedException("오직 MANAGER만 멤버 목록을 조회할 수 있습니다.");
        }
        return fridge.getFridgeMemberList().stream()
                .map(FridgeMemberResDto::fromEntity)
                .collect(Collectors.toList());
    }

    public FridgeListDto fridgeDetail(Long fridgeId) {
        FridgeAccessValidator.FridgeContext context =  fridgeAccessValidator.validate(fridgeId);
        return FridgeListDto.fromEntity(context.fridge(),context.type());
    }

    public List<FridgeListDto> findMyFridges() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("member is not found"));

        List<FridgeMember> fridgeMembers = fridgeMemberRepository.findAllByMember(member);

        return fridgeMembers.stream()
                .map(fridgeMember -> FridgeListDto.from(fridgeMember))
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

        managerChatRoomLifecycle.createManagerChatRoom(member, fridge);

        Member receiver = fridgeMemberRepository.findByFridgeAndType(fridge, Type.MANAGER)
                .orElseThrow(() -> new EntityNotFoundException("")).getMember();

        // 알림 발송 + db 저장
        notificationService.create(member, receiver, NotificationType.NEW_MEMBER, fridge);

        return fridge.getId();
    }

    @Transactional
    public Long deleteFridge(Long fridgeId) {
        FridgeAccessValidator.FridgeContext context = fridgeAccessValidator.validate(fridgeId);
        if(!context.type().equals(Type.MANAGER)){
            throw new AccessDeniedException("MANAGER만 삭제할 수 있습니다");
        } else {
            fridgeRepository.deleteById(context.fridge().getId());
        }
        return context.fridge().getId();
    }

    @Transactional
    public Long leaveFridge(Long fridgeId){
        FridgeAccessValidator.FridgeContext context = fridgeAccessValidator.validate(fridgeId);
        if(!context.type().equals(Type.COMMON)){
            throw new AccessDeniedException("COMMON만 나갈수 있습니다");
        } else {
            fridgeMemberRepository.deleteByFridgeAndMember(context.fridge(), context.member());
            foodRepository.deleteAllByFridgeAndMember(context.fridge(), context.member());
            postRepository.deleteAllByFridgeAndMember(context.fridge(), context.member());
        }
        return context.fridge().getId();
    }

    @Transactional
    public void delegateManager(Long fridgeId, UUID memberId) {
        FridgeAccessValidator.FridgeContext context = fridgeAccessValidator.validate(fridgeId);
        if (context.type() != Type.MANAGER) {
            throw new AccessDeniedException("오직 MANAGER만 권한을 위임할 수 있습니다.");
        }
        Fridge fridge = context.fridge();
        Member currentManager = context.member();

        Member newManager = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("새로운 매니저로 지정할 사용자를 찾을 수 없습니다."));

        FridgeMember currentFridgeManager = fridgeMemberRepository.findByFridgeAndMember(fridge, currentManager)
                .orElseThrow(() -> new EntityNotFoundException("현재 매니저 정보를 찾을 수 없습니다."));
        currentFridgeManager.setType(Type.COMMON);

        FridgeMember newFridgeManager = fridgeMemberRepository.findByFridgeAndMember(fridge, newManager)
                .orElseThrow(() -> new AccessDeniedException("새로운 매니저가 해당 냉장고의 멤버가 아닙니다."));
        newFridgeManager.setType(Type.MANAGER);
    }

    @Transactional
    public void removeMember(Long fridgeId, UUID memberId) {
        FridgeAccessValidator.FridgeContext context = fridgeAccessValidator.validate(fridgeId);

        if (context.type() != Type.MANAGER) {
            throw new AccessDeniedException("오직 MANAGER만 멤버를 삭제할 수 있습니다.");
        }

        if (context.member().getId().equals(memberId)) {
            throw new IllegalArgumentException("MANAGER는 자기 자신을 삭제할 수 없습니다.");
        }

        Fridge fridge = context.fridge();

        Member memberToDelete = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 사용자를 찾을 수 없습니다."));

        FridgeMember fridgeMemberToDelete = fridgeMemberRepository.findByFridgeAndMember(fridge, memberToDelete)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 멤버가 냉장고에 존재하지 않습니다."));

        fridgeMemberRepository.delete(fridgeMemberToDelete);
    }
}
