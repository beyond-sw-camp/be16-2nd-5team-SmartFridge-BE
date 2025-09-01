package com.be16_2nd.SmartFridge.member.domain;

import com.be16_2nd.SmartFridge.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;
    @Column(length = 20)
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

    private String profileImage;

    private LocalDateTime lastLoginTime;

    public void updatePw(String password) {
        this.password = password;
    }

    public void updateName(String name){
        this.name = name;
    }

    public void updateProfileImage(String newProfileImage){
        this.profileImage = newProfileImage;
    }
}
