package com.be16_2nd.SmartFridge.member.dto;

import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberResDto {
    private UUID id;
    private String name;
    private String email;
    private Role role;
    private String profileImage;
    private LocalDateTime createdTime;
    private LocalDateTime lastLoginTime;

    public static MemberResDto fromEntity(Member member){
        return MemberResDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .profileImage(member.getProfileImage())
                .createdTime(member.getCreatedTime())
                .lastLoginTime(member.getLastLoginTime())
                .build();
    }
}
