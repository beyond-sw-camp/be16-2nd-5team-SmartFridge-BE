package com.be16_2nd.SmartFridge.food.repository;

import com.be16_2nd.SmartFridge.food.domain.Food;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    Page<Food> findAll(Specification<Food> specification, Pageable pageable);
    void deleteAllByFridgeAndMember(Fridge fridge, Member member);
    @Query("SELECT f FROM Food f WHERE FUNCTION('DATE', f.expirationDateTime) IN :dates")
    List<Food> findAllByExpirationDateIn(@Param("dates") List<LocalDate> dates);

    // --- MANAGER용 메서드 (isTemp 조건 제거) ---
    long countByFridgeId(Long fridgeId);
    long countByFridgeIdAndExpirationDateTimeBefore(Long fridgeId, LocalDateTime now);
    long countByFridgeIdAndExpirationDateTimeBetween(Long fridgeId, LocalDateTime start, LocalDateTime end);

    // --- COMMON 사용자용 메서드 (isTemp 조건 제거) ---
    long countByFridgeIdAndMemberId(Long fridgeId, UUID userId);
    long countByFridgeIdAndMemberIdAndExpirationDateTimeBefore(Long fridgeId, UUID userId, LocalDateTime now);
    long countByFridgeIdAndMemberIdAndExpirationDateTimeBetween(Long fridgeId, UUID userId, LocalDateTime start, LocalDateTime end);

}
