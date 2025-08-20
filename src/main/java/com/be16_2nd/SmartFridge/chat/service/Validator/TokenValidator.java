package com.be16_2nd.SmartFridge.chat.service.Validator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenValidator {

    @Value("${jwt.secretKeyAt}")
    private String secretKey;

    public Claims validateToken(String bearerToken) {
        String token = bearerToken.substring(7);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }
}
