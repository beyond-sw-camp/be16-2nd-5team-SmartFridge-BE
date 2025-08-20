package com.be16_2nd.SmartFridge.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NaverRedirectDto {
    private String state;
    private String code;
}
