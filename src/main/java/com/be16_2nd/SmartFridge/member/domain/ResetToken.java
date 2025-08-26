package com.be16_2nd.SmartFridge.member.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetToken {
    private String token;
    private String email;


}
