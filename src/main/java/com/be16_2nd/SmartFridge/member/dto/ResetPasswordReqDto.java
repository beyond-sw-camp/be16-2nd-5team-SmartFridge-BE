package com.be16_2nd.SmartFridge.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordReqDto {
    private String token;
    private String newPassword;

}
