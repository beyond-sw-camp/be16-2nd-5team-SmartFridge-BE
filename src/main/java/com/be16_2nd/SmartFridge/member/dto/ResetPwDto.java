package com.be16_2nd.SmartFridge.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPwDto {
    private String resetToken;
    private String newPassword;
    private String newPasswordConfirm;
}
