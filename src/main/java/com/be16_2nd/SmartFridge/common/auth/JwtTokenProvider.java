package com.be16_2nd.SmartFridge.common.auth;

import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.expirationAt}")
    private long expiration;

    @Value("${jwt.secretKeyAt}")
    private String secretKeyAt;

    @Value("${jwt.expirationRt}")
    private int expirationRt;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private SecretKey secret_at_key;
    private SecretKey secret_rt_key;

    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtTokenProvider(MemberRepository memberRepository, RedisTemplate<String, String> redisTemplate) {
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init(){
        secret_at_key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyAt));
        secret_rt_key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyRt));

    }

    public String createSocialToken(String email, String role){
        Date now = new Date();
        String token = Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime()+expiration*60*1000))
                .signWith(secret_at_key)
                .compact();
        return token;
    }

    public String createAtToken(Member member){
        String email = member.getEmail();
        String role = member.getRole().toString();
        Date now = new Date();
        String token = Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime()+expiration*60*1000))
                .signWith(secret_at_key)
                .compact();
        return token;
    }

    public String createRtToken(Member member) {
        // 유효 기간이 긴 rt 토큰 생성
        String email = member.getEmail();
        String role = member.getRole().toString();
        init();

        Date now = new Date();
        String refreshToken = Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationRt * 90 * 1000L))
                .signWith(secret_rt_key)
                .compact();


        // rt 토큰을 redis에 저장 : key-value 형식으로 set
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken);
        // 200일 TTL(유효 시간)
//        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 200, TimeUnit.DAYS);
        return refreshToken;
    }

    public Member validateRt(String refreshToken) {
        // rt 그 자체를 검증
        Claims claims = Jwts.parser()
                .verifyWith(secret_rt_key)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        String email = claims.getSubject();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member is not found"));

        // redis의 값과 비교하는 검증
        String redisRt = redisTemplate.opsForValue().get(member.getEmail());
        if (!redisRt.equals(refreshToken)) {
            throw new IllegalArgumentException("잘못된 토큰 입니다.");
        }

        return member;
    }
}
