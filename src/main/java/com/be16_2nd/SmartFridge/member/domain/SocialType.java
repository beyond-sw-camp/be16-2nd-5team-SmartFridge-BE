package com.be16_2nd.SmartFridge.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialType {
    KAKAO("카카오"), NAVER("네이버"), GOOGLE("구글");

    private final String displayName;
}
