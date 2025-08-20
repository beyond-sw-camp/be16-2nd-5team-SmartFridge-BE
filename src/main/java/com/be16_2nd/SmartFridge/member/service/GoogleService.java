package com.be16_2nd.SmartFridge.member.service;

import com.be16_2nd.SmartFridge.member.dto.AccessTokenDto;
import com.be16_2nd.SmartFridge.member.dto.GoogleProfileDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@Transactional
public class GoogleService {

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    public AccessTokenDto getAccessToken(String code){
// 인가코드, clientId, client_secret, redirect_uri, grant_type
//        spring 6부터 RestTemplate 비추천상태이기에 대신 RestClient사용
        RestClient restClient = RestClient.create();

//        MultiValueMap을 통해 자동으로 form-data형식으로 body 조립 가능
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<AccessTokenDto> response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
//                ?code=xxxx&client_id=yyyy&...
                .body(params)
//                retrieve:응답 body값 만을 추출
                .retrieve()
                .toEntity(AccessTokenDto.class);

        return response.getBody();

    }

    public GoogleProfileDto getGoogleProfile(String token){
        RestClient restClient = RestClient.create();
        ResponseEntity<GoogleProfileDto> response = restClient.post()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header("Authorization", "Bearer "+ token)
                .retrieve()
                .toEntity(GoogleProfileDto.class);

        return response.getBody();
    }
}
