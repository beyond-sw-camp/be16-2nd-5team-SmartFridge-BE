package com.be16_2nd.SmartFridge.member.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberInfoDto {
        private String id;
        private String name;
        private String email;
        private String profileImage;
        private String role;
}
