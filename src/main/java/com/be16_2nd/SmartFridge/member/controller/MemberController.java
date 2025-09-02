package com.be16_2nd.SmartFridge.member.controller;

import com.be16_2nd.SmartFridge.common.auth.JwtTokenProvider;
import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import com.be16_2nd.SmartFridge.email.dto.EmailDto;
import com.be16_2nd.SmartFridge.email.service.EmailService;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import com.be16_2nd.SmartFridge.member.dto.*;
import com.be16_2nd.SmartFridge.member.service.GoogleService;
import com.be16_2nd.SmartFridge.member.service.KakaoService;
import com.be16_2nd.SmartFridge.member.service.MemberService;
import com.be16_2nd.SmartFridge.member.service.NaverService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleService googleService;
    private final KakaoService kakaoService;
    private final NaverService naverService;
    private final EmailService emailService;

    @PostMapping("/create")
    public ResponseEntity<?> save(@RequestBody @Valid MemberCreateDto memberCreateDto){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberService.save(memberCreateDto))
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

    @GetMapping("/info")
    public ResponseEntity<?> getMemberInfo() {
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberService.myInfo())
                        .status_code(HttpStatus.OK.value())
                        .status_message("사용자 정보 조회 성공")
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
            originalMember = memberService.createOauth(googleProfileDto.getSub(),
                    googleProfileDto.getEmail(),
                    googleProfileDto.getName(),
                    googleProfileDto.getPicture(),
                    SocialType.GOOGLE);
        }
//        회원 가입이 되어있는 회원이라면 토큰 발급
        String jwtToken = jwtTokenProvider.createSocialToken(originalMember.getEmail(),
                originalMember.getRole().toString());

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

    //    kakao로그인 메서드
    @PostMapping("/kakao/doLogin")
    public ResponseEntity<?> kakaoLogin(@RequestBody RedirectDto redirectDto){
        AccessTokenDto accessTokenDto = kakaoService.getAccessToken(redirectDto.getCode());
        KakaoProfileDto kakaoProfileDto = kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());
        Member originalMember = memberService.getMemberBySocialId(kakaoProfileDto.getId());
        if (originalMember == null){
            originalMember = memberService.createOauth(kakaoProfileDto.getId(),
                    kakaoProfileDto.getKakao_account().getEmail(),
                    kakaoProfileDto.getKakao_account().getProfile().getNickname(),
                    kakaoProfileDto.getKakao_account().getProfile().getProfile_image_url(),
                    SocialType.KAKAO);
        }
        String jwtToken = jwtTokenProvider.createSocialToken(originalMember.getEmail(),
                originalMember.getRole().toString());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", originalMember.getId());
        loginInfo.put("token", jwtToken);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(loginInfo)
                        .status_code(HttpStatus.OK.value())
                        .status_message("카카오 로그인 성공")
                        .build(), HttpStatus.OK);
    }

    //    naver로그인 메서드
    @PostMapping("/naver/doLogin")
    public ResponseEntity<?> naverLogin(@RequestBody NaverRedirectDto naverRedirectDto){
        AccessTokenDto accessTokenDto = naverService.getAccessToken(naverRedirectDto.getCode(), naverRedirectDto.getState());
        NaverProfileDto naverProfileDto = naverService.getNaverProfile(accessTokenDto.getAccess_token());
        Member originalMember = memberService.getMemberBySocialId(naverProfileDto.getResponse().getId());
        if (originalMember == null){
            originalMember = memberService.createOauth(naverProfileDto.getResponse().getId(),
                    naverProfileDto.getResponse().getEmail(),
                    naverProfileDto.getResponse().getName(),
                    naverProfileDto.getResponse().getProfile_image(),
                    SocialType.NAVER);
        }
        String jwtToken = jwtTokenProvider.createSocialToken(originalMember.getEmail(),
                originalMember.getRole().toString());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", originalMember.getId());
        loginInfo.put("token", jwtToken);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(loginInfo)
                        .status_code(HttpStatus.OK.value())
                        .status_message("네이버 로그인 성공")
                        .build(), HttpStatus.OK);
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

//    비밀번호 재설정 이메일 전송
    @PostMapping("/forgot-password/send-code")
    public ResponseEntity<?> mailSend(@RequestBody EmailDto emailDto) throws MessagingException {
        emailService.sendEmail(emailDto.getMail());
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result("OK")
                        .status_code(HttpStatus.OK.value())
                        .status_message("인증코드가 발송되었습니다.")
                        .build(),HttpStatus.OK);
    }

//    비밀번호 재설정 이메일 검증
    @PostMapping("/forgot-password/verify-code")
    public ResponseEntity<?> verify(@RequestBody EmailDto emailDto) {

        boolean isVerify = emailService.verifyEmailCode(emailDto.getMail(), emailDto.getVerifyCode());
        if (isVerify){
            return new ResponseEntity<>(
                    CommonDto.builder()
                            .result("VERIFIED")
                            .status_code(HttpStatus.OK.value())
                            .status_message("이메일 인증이 완료되었습니다.")
                            .build(),HttpStatus.OK);
        }else {
            return new ResponseEntity<>(
                    CommonDto.builder()
                            .result("UNVERIFIED")
                            .status_code(HttpStatus.BAD_REQUEST.value())
                            .status_message("이메일 인증 실패하셨습니다.")
                            .build(),HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/forgot-password/updatepw")
    public ResponseEntity<?> updatePw(@RequestBody UpdatePwDto updatePwDto){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberService.updatePw(updatePwDto))
                        .status_code(HttpStatus.OK.value())
                        .status_message("비밀번호가 성공적으로 변경되었습니다.")
                        .build(),HttpStatus.OK);
    }

    @GetMapping("/myposts")
    public ResponseEntity<?> myposts(){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberService.myposts())
                        .status_code(HttpStatus.OK.value())
                        .status_message("내 게시 목록 조회 성공")
                        .build(),HttpStatus.OK);
    }

    @GetMapping("/mystats")
    public ResponseEntity<?> mystats(){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberService.mystats())
                        .status_code(HttpStatus.OK.value())
                        .status_message("내 활동 통계 조회 성공")
                        .build(),HttpStatus.OK);
    }

    @PutMapping("/update/name")
    public ResponseEntity<?> updateMyName(@RequestBody UpdateNameDto updateNameDto){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberService.updateName(updateNameDto))
                        .status_code(HttpStatus.OK.value())
                        .status_message("이름이 변경되었습니다.")
                        .build(),HttpStatus.OK);
    }

    @PutMapping("/update/profileimage")
    public ResponseEntity<?> updateMyProfileImage(@ModelAttribute UpdateProfielImageDto updateProfielImageDto) throws IOException {
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberService.updateMyProfileImage(updateProfielImageDto))
                        .status_code(HttpStatus.OK.value())
                        .status_message("프로필 이미지가 변경되었습니다.")
                        .build(),HttpStatus.OK);
    }

    // RT를 통한 AT 갱신 요청
    @PostMapping("/refresh-at")
    public ResponseEntity<?> generateNewAt(@RequestBody @Valid RefreshTokenDto refreshTokenDto){
        // RT 검증 로직
        Member member = jwtTokenProvider.validateRt(refreshTokenDto.getRefreshToken());

        // AT 신규 생성 로직
        String accessToken = jwtTokenProvider.createAtToken(member);

        LoginResDto loginResDto = LoginResDto.builder()
                .accessToken(accessToken)
                .build();

        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(loginResDto)
                        .status_code(
                                HttpStatus.OK.value())
                        .status_message("accessToken 재발급 성공!")
                        .build(),
                HttpStatus.OK);
    }
}
