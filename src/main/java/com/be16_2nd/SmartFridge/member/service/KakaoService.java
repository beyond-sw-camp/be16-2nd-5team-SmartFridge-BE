package com.be16_2nd.SmartFridge.member.service;

import com.be16_2nd.SmartFridge.member.dto.AccessTokenDto;
import com.be16_2nd.SmartFridge.member.dto.KakaoProfileDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@Transactional
public class KakaoService {

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    public AccessTokenDto getAccessToken(String code){
        RestClient restClient = RestClient.create();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<AccessTokenDto> response = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .toEntity(AccessTokenDto.class);

        return response.getBody();

    }

    public KakaoProfileDto getKakaoProfile(String token){
        RestClient restClient = RestClient.create();
        ResponseEntity<KakaoProfileDto> response = restClient.post()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer "+ token)
                .retrieve()
                .toEntity(KakaoProfileDto.class);
        System.out.println("profile JSON " + response.getBody());
        return response.getBody();
    }
}
