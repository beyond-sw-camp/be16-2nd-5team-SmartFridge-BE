package com.be16_2nd.SmartFridge.food.service;

import com.be16_2nd.SmartFridge.common.service.FridgeAccessValidator;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.domain.Type;
import com.be16_2nd.SmartFridge.food.domain.Food;
import com.be16_2nd.SmartFridge.food.dto.*;
import com.be16_2nd.SmartFridge.food.repository.FoodRepository;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.notification.domain.NotificationType;
import com.be16_2nd.SmartFridge.notification.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final FridgeAccessValidator fridgeAccessValidator;
    private final NotificationService notificationService;
    private final FridgeMemberRepository fridgeMemberRepository;

    public FoodResDto registerFood(Long fridgeId, FoodCreateDto foodCreateDto) {
        FridgeAccessValidator.FridgeContext context = fridgeAccessValidator.validate(fridgeId);
        Food food = foodCreateDto.toEntity(context.member(), context.fridge());
        Food savedFood = foodRepository.save(food);

        Fridge fridge = context.fridge();
        Member receiver = fridgeMemberRepository.findByFridgeAndType(fridge, Type.MANAGER)
                .orElseThrow(() -> new EntityNotFoundException("")).getMember();
        Member sender = context.member();

        // 식자재 등록 알림 (사용자 -> 냉장고 관리자)
        notificationService.create(sender, receiver, NotificationType.NEW_FOOD, food);

        return FoodResDto.fromEntity(savedFood);
    }

    @Transactional(readOnly = true)
    public Page<FoodResDto> getFoodsByRole(Long fridgeId, Pageable pageable, FoodSearchDto foodSearchDto, Boolean isShared) {
        FridgeAccessValidator.FridgeContext context = fridgeAccessValidator.validate(fridgeId);

        Specification<Food> specification = createFoodSpecification(fridgeId, context.type(), context.member(), isShared, foodSearchDto);

        return foodRepository.findAll(specification, pageable).map(FoodResDto::fromEntity);
    }


    public FoodResDto updateFood(Long fridgeId, Long foodId, FoodUpdateDto foodUpdateDto) {
        fridgeAccessValidator.validate(fridgeId);
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new EntityNotFoundException("없는 식품입니다. ID: " + foodId));
        if (!food.getFridge().getId().equals(fridgeId)) {
            throw new IllegalArgumentException("해당 냉장고에 존재하지 않는 식품입니다.");
        }
        Food updatedFood = food.updateFood(foodUpdateDto);
        return FoodResDto.fromEntity(updatedFood);
    }

    public void deleteFood(Long fridgeId, Long foodId) {
        FridgeAccessValidator.FridgeContext context = fridgeAccessValidator.validate(fridgeId);
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 식품을 찾을 수 없습니다. ID: " + foodId));
        if (!food.getFridge().getId().equals(fridgeId)) {
            throw new IllegalArgumentException("해당 냉장고에 존재하지 않는 식품입니다.");
        }
        if (context.type() != Type.MANAGER && !food.getMember().getId().equals(context.member().getId())) {
            throw new IllegalArgumentException("본인이 등록한 식품만 삭제할 수 있습니다.");
        }
        foodRepository.delete(food);
    }

        @Transactional(readOnly = true)
        public FoodStatResDto foodStats(Long fridgeId) {

            FridgeAccessValidator.FridgeContext context = fridgeAccessValidator.validate(fridgeId);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime threeDaysLater = now.plusDays(3);

            long totalCount;
            long expiredCount;
            long expiringSoonCount;

            // 사용자의 역할을 확인합니다.
            if (context.type().equals("MANAGER")) {
                // MANAGER는 냉장고의 모든 음식 통계를 조회합니다. (임시 등록 포함)
                totalCount = foodRepository.countByFridgeId(fridgeId);
                expiredCount = foodRepository.countByFridgeIdAndExpirationDateTimeBefore(fridgeId, now);
                expiringSoonCount = foodRepository.countByFridgeIdAndExpirationDateTimeBetween(fridgeId, now, threeDaysLater);
            } else {
                // COMMON 사용자는 자신이 등록한 음식 통계만 조회합니다. (임시 등록 포함)
                UUID memberId = context.member().getId();
                totalCount = foodRepository.countByFridgeIdAndMemberId(fridgeId, memberId);
                expiredCount = foodRepository.countByFridgeIdAndMemberIdAndExpirationDateTimeBefore(fridgeId, memberId, now);
                expiringSoonCount = foodRepository.countByFridgeIdAndMemberIdAndExpirationDateTimeBetween(fridgeId, memberId, now, threeDaysLater);
            }

            long freshCount = totalCount - expiredCount - expiringSoonCount;

            return FoodStatResDto.builder()
                    .totalCount(totalCount)
                    .freshCount(freshCount)
                    .expiringSoonCount(expiringSoonCount)
                    .expiredCount(expiredCount)
                    .build();
        }

    private Specification<Food> createFoodSpecification(Long fridgeId, Type userType, Member member, Boolean isShared, FoodSearchDto searchDto) {
        return (root, query, cb) -> {
            List<Predicate> predicateList = new ArrayList<>();
            predicateList.add(cb.equal(root.get("fridge").get("id"), fridgeId));

            if (userType == Type.COMMON) {
                Predicate isSharedPredicate = cb.equal(root.get("isShared"), true);
                Predicate isMyFoodPredicate = cb.equal(root.get("member"), member);
                predicateList.add(cb.or(isSharedPredicate, isMyFoodPredicate));
            }

            if (isShared != null) {
                predicateList.add(cb.equal(root.get("isShared"), isShared));
            }
            if (searchDto.getCategory() != null) {
                predicateList.add(cb.equal(root.get("category"), searchDto.getCategory()));
            }
            if (StringUtils.hasText(searchDto.getFoodName())) {
                predicateList.add(cb.like(root.get("name"), "%" + searchDto.getFoodName() + "%"));
            }
            // 유통기한 및 임시보관 필터
            addFilterPredicate(predicateList, searchDto.getFilterType(), cb, root);

            return cb.and(predicateList.toArray(new Predicate[0]));
        };
    }

    private void addFilterPredicate(List<Predicate> predicates, FoodFilterType filterType, CriteriaBuilder cb, Root<Food> root) {
        if (filterType == null || filterType == FoodFilterType.ALL) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        switch (filterType) {
            case TEMP_ONLY:
                predicates.add(cb.equal(root.get("isTemp"), true));
                break;
            case EXPIRED:
                predicates.add(cb.equal(root.get("isTemp"), false));
                predicates.add(cb.lessThan(root.get("expirationDateTime"), now));
                break;
            case EXPIRES_TODAY:
                predicates.add(cb.equal(root.get("isTemp"), false));
                LocalDateTime startOfToday = today.atStartOfDay();
                LocalDateTime endOfToday = today.plusDays(1).atStartOfDay();
                predicates.add(cb.between(root.get("expirationDateTime"), startOfToday, endOfToday));
                break;
            case EXPIRES_IN_THREE_DAYS:
                predicates.add(cb.equal(root.get("isTemp"), false));
                LocalDateTime threeDaysLater = today.plusDays(3).atStartOfDay();
                predicates.add(cb.between(root.get("expirationDateTime"), now, threeDaysLater));
                break;
        }
    }
}