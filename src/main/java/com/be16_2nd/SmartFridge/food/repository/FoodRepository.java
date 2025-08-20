package com.be16_2nd.SmartFridge.food.repository;

import com.be16_2nd.SmartFridge.food.domain.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    Page<Food> findAll(Specification<Food> specification, Pageable pageable);

    @Query("SELECT f FROM Food f WHERE FUNCTION('DATE', f.expirationDateTime) IN :dates")
    List<Food> findAllByExpirationDateIn(@Param("dates") List<LocalDate> dates);

}
