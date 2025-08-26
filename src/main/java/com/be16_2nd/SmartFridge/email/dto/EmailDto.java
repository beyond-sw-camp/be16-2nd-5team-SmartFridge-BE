package com.be16_2nd.SmartFridge.email.dto;

import lombok.Data;

@Data
public class EmailDto {

    // 이메일 주소
    private String mail;
    // 인증 코드
    private String verifyCode;
}