package com.be16_2nd.SmartFridge.member.controller;

import com.be16_2nd.SmartFridge.common.auth.JwtTokenProvider;
import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import com.be16_2nd.SmartFridge.common.dto.CommonErrorDto;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import com.be16_2nd.SmartFridge.member.dto.*;
import com.be16_2nd.SmartFridge.member.service.GoogleService;
import com.be16_2nd.SmartFridge.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleService googleService;

    @PostMapping("/create")
    public ResponseEntity<?> save(@RequestBody @Valid MemberCreateDto memberCreateDto){
        UUID id = memberService.save(memberCreateDto);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(id)
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("회원가입을 축하합니다!")
                        .build(),HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody LoginReqDto loginReqDto){
        Member member = memberService.doLogin(loginReqDto);
        String accessToken = jwtTokenProvider.createAtToken(member);
        String refreshToken = jwtTokenProvider.createRtToken(member);

        LoginResDto loginResDto = LoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(loginResDto)
                        .status_code(HttpStatus.OK.value())
                        .status_message("로그인 성공")
                        .build(), HttpStatus.OK);
    }

//    google로그인 메서드
    @PostMapping("/google/doLogin")
    public ResponseEntity<?> googleLogin(@RequestBody RedirectDto redirectDto){
//        accessToken 발급
        AccessTokenDto accessTokenDto = googleService.getAccessToken(redirectDto.getCode());
//        사용자 정보 얻기
        GoogleProfileDto googleProfileDto = googleService.getGoogleProfile(accessTokenDto.getAccess_token());
//        회원가입 되어있지 않다면 회원 가입
        Member originalMember = memberService.getMemberBySocialId(googleProfileDto.getSub());
        if (originalMember == null){
            originalMember = memberService.createOauth(googleProfileDto.getSub(), googleProfileDto.getEmail(), SocialType.GOOGLE);
        }
//        회원 가입이 되어있는 회원이라면 토큰 발급
        String jwtToken = jwtTokenProvider.createAtToken(originalMember);

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", originalMember.getId());
        loginInfo.put("token", jwtToken);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(loginInfo)
                        .status_code(HttpStatus.OK.value())
                        .status_message("구글 로그인 성공")
                        .build(), HttpStatus.OK);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAll(){
        List<MemberResDto> memberResDtoList = memberService.findAll();
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberResDtoList)
                        .status_code(HttpStatus.OK.value())
                        .status_message("회원목록 조회 완료")
                        .build(),HttpStatus.OK);
    }

    @GetMapping("/myinfo")
    public ResponseEntity<?> myinfo(){

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberService.myInfo())
                        .status_code(HttpStatus.OK.value())
                        .status_message("내 정보 페이지입니다.")
                        .build(),HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(){
        memberService.delete();
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result("OK")
                        .status_code(HttpStatus.OK.value())
                        .status_message("회원탈퇴가 성공적으로 되었습니다. \n"  +
                                "그동안 저희 Smart Fridge를 이용해주셔서 감사합니다.")
                        .build(), HttpStatus.OK);
    }
}
