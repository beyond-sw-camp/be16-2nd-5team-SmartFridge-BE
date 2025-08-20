package com.be16_2nd.SmartFridge.food.domain;

import com.be16_2nd.SmartFridge.food.dto.FoodUpdateDto;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private Category category;
    @Enumerated(EnumType.STRING)
    private StorageType storageType;

    @Column(nullable = false)
    private LocalDate storageStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime expirationDateTime;

    private String memo;
    @Column(nullable = false)
    private Boolean isShared;
    @Column(nullable = false)
    private Boolean isTemp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public Food updateFood(FoodUpdateDto foodUpdateDto) {
        LocalDateTime expirationDateTime = foodUpdateDto.getExpirationDate().atTime(foodUpdateDto.getExpirationHour(), foodUpdateDto.getExpirationMinute());
        this.name = foodUpdateDto.getName();
        this.quantity = foodUpdateDto.getQuantity();
        this.storageType = foodUpdateDto.getStorageType();
        this.category =  foodUpdateDto.getCategory();
        this.expirationDateTime = expirationDateTime;
        this.memo =  foodUpdateDto.getMemo();
        this.isShared = foodUpdateDto.getShareable();
        this.isTemp = foodUpdateDto.getIsTemp();
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fridge_id")
    private Fridge fridge;
}
