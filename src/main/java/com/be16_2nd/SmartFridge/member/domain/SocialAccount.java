package com.be16_2nd.SmartFridge.member.domain;

import com.be16_2nd.SmartFridge.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

// 한 회원에 여러 소셜 계정(구글/카카오/네이버)을 연결하기 위한 엔티티
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_social_account_type_id",
        columnNames = {"social_type", "social_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SocialAccount extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = false, length = 20)
    private SocialType socialType;

    @Column(name = "social_id", nullable = false)
    private String socialId;
}
