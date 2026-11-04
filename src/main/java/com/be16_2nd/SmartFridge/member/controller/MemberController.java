package com.be16_2nd.SmartFridge.member.controller;

import com.be16_2nd.SmartFridge.common.auth.JwtTokenProvider;
import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import com.be16_2nd.SmartFridge.member.dto.*;
import com.be16_2nd.SmartFridge.member.service.GoogleService;
import com.be16_2nd.SmartFridge.member.service.KakaoService;
import com.be16_2nd.SmartFridge.member.service.MemberService;
import com.be16_2nd.SmartFridge.member.service.NaverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleService googleService;
    private final KakaoService kakaoService;
    private final NaverService naverService;

    // 관리자 전용 일반 로그인 (일반 회원은 소셜 로그인만 사용)
    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody LoginReqDto loginReqDto){
        Member member = memberService.doAdminLogin(loginReqDto);
        return loginResponse(member, "로그인 성공");
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
        AccessTokenDto accessTokenDto = googleService.getAccessToken(redirectDto.getCode());
        GoogleProfileDto profile = googleService.getGoogleProfile(accessTokenDto.getAccess_token());
        Member member = memberService.socialLogin(SocialType.GOOGLE,
                profile.getSub(),
                profile.getEmail(),
                Boolean.TRUE.equals(profile.getEmail_verified()),
                profile.getName(),
                profile.getPicture());
        return loginResponse(member, "구글 로그인 성공");
    }

    //    kakao로그인 메서드
    @PostMapping("/kakao/doLogin")
    public ResponseEntity<?> kakaoLogin(@RequestBody RedirectDto redirectDto){
        AccessTokenDto accessTokenDto = kakaoService.getAccessToken(redirectDto.getCode());
        KakaoProfileDto profile = kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());
        KakaoProfileDto.KakaoAccount account = profile.getKakao_account();
        KakaoProfileDto.Profile kakaoProfile = account != null ? account.getProfile() : null;
        Member member = memberService.socialLogin(SocialType.KAKAO,
                profile.getId(),
                account != null ? account.getEmail() : null,
                account != null && Boolean.TRUE.equals(account.getIs_email_valid())
                        && Boolean.TRUE.equals(account.getIs_email_verified()),
                kakaoProfile != null ? kakaoProfile.getNickname() : null,
                kakaoProfile != null ? kakaoProfile.getProfile_image_url() : null);
        return loginResponse(member, "카카오 로그인 성공");
    }

    //    naver로그인 메서드
    @PostMapping("/naver/doLogin")
    public ResponseEntity<?> naverLogin(@RequestBody NaverRedirectDto naverRedirectDto){
        AccessTokenDto accessTokenDto = naverService.getAccessToken(naverRedirectDto.getCode(), naverRedirectDto.getState());
        NaverProfileDto.Response profile = naverService.getNaverProfile(accessTokenDto.getAccess_token()).getResponse();
        // 네이버는 인증 여부 필드를 주지 않음. 네이버가 확인한 계정 이메일로 보고 연결을 허용
        Member member = memberService.socialLogin(SocialType.NAVER,
                profile.getId(),
                profile.getEmail(),
                true,
                profile.getName(),
                profile.getProfile_image());
        return loginResponse(member, "네이버 로그인 성공");
    }

    // 일반/소셜 로그인 공통: AT + RT 발급
    private ResponseEntity<?> loginResponse(Member member, String message){
        LoginResDto loginResDto = LoginResDto.builder()
                .accessToken(jwtTokenProvider.createAtToken(member))
                .refreshToken(jwtTokenProvider.createRtToken(member))
                .build();
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(loginResDto)
                        .status_code(HttpStatus.OK.value())
                        .status_message(message)
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

    @GetMapping("/mynoti")
    public ResponseEntity<?> mySettings(){
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(memberService.mySettings())
                        .status_code(HttpStatus.OK.value())
                        .status_message("알림 설정 조회 완료")
                        .build(),HttpStatus.OK);
    }
}
