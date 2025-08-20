package com.be16_2nd.SmartFridge.member.domain;

import com.be16_2nd.SmartFridge.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
@Where(clause = "del_yn='N'")
@Setter
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;
    @Column(length = 10)
    private String name;
    @Column(length = 50, unique = true)
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SocialType socialType = SocialType.LOCAL;

    private String socialId;

    @Builder.Default
    private String delYn = "N";

    @Builder.Default
    private LocalDateTime withdrawalDate = null;

    private String profileImage;


    public void delete(Member member){
        this.delYn = "Y";
        this.setWithdrawalDate(LocalDateTime.now());
    }
}
