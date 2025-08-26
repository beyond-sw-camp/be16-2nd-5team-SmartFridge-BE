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
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
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
    private final KakaoService kakaoService;
    private final NaverService naverService;

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
    public ResponseEntity<?> getMemberInfo(@RequestParam String email) {
        try {

            Member member = memberService.findByEmail(email);

            if (member != null) {

                return new ResponseEntity<>(
                        CommonDto.builder()
                                .result(member)
                                .status_code(HttpStatus.OK.value())
                                .status_message("사용자 정보 조회 성공")
                                .build(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(
                        CommonDto.builder()
                                .result(null)
                                .status_code(HttpStatus.NOT_FOUND.value())
                                .status_message("사용자를 찾을 수 없습니다.")
                                .build(), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();

            return new ResponseEntity<>(
                    CommonDto.builder()
                            .result(null)
                            .status_code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .status_message("사용자 정보 조회 중 오류가 발생했습니다: " + e.getMessage())
                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
                    naverProfileDto.getResponse().getNickname(),
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
//    @PostMapping("/member/forgot-password-code")
//    public ResponseEntity<?> sendPasswordResetCode(@RequestBody ForgotPasswordReqDto dto) throws MessagingException {
//        memberService.sendResetToken(dto);
//        return new ResponseEntity<>(
//                CommonDto.builder()
//                        .result(true)
//                        .status_code(HttpStatus.OK.value())
//                        .status_message("비밀번호 재설정 코드가 이메일로 전송되었습니다.")
//                        .build(), HttpStatus.OK);
//    }
//
//    public ResponseEntity<?> verifyPasswordResetCode(@RequestBody VerifyPasswordResetCodeReqDto dto) {
//        try {
//            String tempToken = memberService.verifyPasswordResetCode(dto.getEmail(), dto.getCode());
//
//            return new ResponseEntity<>(
//                    CommonDto.builder()
//                            .result(true)
//                            .status_code(HttpStatus.OK.value())
//                            .status_message("코드 검증이 완료되었습니다.")
//                            .data(Map.of("tempToken", tempToken)) // 임시 토큰 반환
//                            .build(), HttpStatus.OK);
//
//        } catch (IllegalArgumentException e) {
//            return new ResponseEntity<>(
//                    CommonDto.builder()
//                            .result(false)
//                            .status_code(HttpStatus.BAD_REQUEST.value())
//                            .status_message(e.getMessage())
//                            .build(), HttpStatus.BAD_REQUEST);
//        } catch (Exception e) {
//            return new ResponseEntity<>(
//                    CommonDto.builder()
//                            .result(false)
//                            .status_code(HttpStatus.INTERNAL_SERVER_ERROR.value())
//                            .status_message("코드 검증 중 오류가 발생했습니다.")
//                            .build(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
}
