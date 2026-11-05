package com.be16_2nd.SmartFridge.member.service;

import com.be16_2nd.SmartFridge.fridge.domain.FridgeMember;
import com.be16_2nd.SmartFridge.fridge.domain.Type;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import com.be16_2nd.SmartFridge.post.domain.Post;
import com.be16_2nd.SmartFridge.post.repository.PostCommentRepository;
import com.be16_2nd.SmartFridge.post.repository.PostLikeRepository;
import com.be16_2nd.SmartFridge.post.repository.PostRepository;
import com.be16_2nd.SmartFridge.common.service.S3Uploader;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.domain.Role;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import com.be16_2nd.SmartFridge.member.dto.*;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import com.be16_2nd.SmartFridge.notification.domain.NotificationSetting;
import com.be16_2nd.SmartFridge.notification.dto.NotificationSettingDto;
import com.be16_2nd.SmartFridge.notification.repository.NotificationSettingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MemberService {


    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Uploader s3Uploader;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final FridgeMemberRepository fridgeMemberRepository;

    // 일반(이메일/비밀번호) 로그인은 관리자 계정만 허용
    public Member doAdminLogin(LoginReqDto loginReqDto){
        Member member = memberRepository.findByEmail(loginReqDto.getEmail())
                .filter(m -> m.getRole() == Role.ADMIN)
                .filter(m -> m.getPassword() != null
                        && passwordEncoder.matches(loginReqDto.getPassword(), m.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));
        return member;
    }

    /**
     * 소셜 로그인 처리
     * 1. 이미 가입한 소셜 계정(social_type + social_id)이면 해당 회원 반환
     * 2. 같은 이메일의 회원이 있으면 자동으로 합치지 않고, 기존에 가입한 소셜 계정으로 로그인하도록 안내
     * 3. 없으면 신규 회원 생성 (인증된 이메일만)
     */
    public Member socialLogin(SocialType socialType, String socialId, String email, boolean emailVerified,
                              String name, String picture){
        Optional<Member> linked = memberRepository.findBySocialTypeAndSocialId(socialType, socialId);
        if (linked.isPresent()){
            return linked.get();
        }

        if (email == null || email.isBlank()){
            throw new IllegalArgumentException("이메일 제공에 동의해야 로그인할 수 있습니다.");
        }

        Optional<Member> sameEmailMember = memberRepository.findByEmail(email);
        if (sameEmailMember.isPresent()){
            Member member = sameEmailMember.get();
            if (member.getRole() == Role.ADMIN){
                throw new IllegalArgumentException("소셜 로그인을 사용할 수 없는 계정입니다.");
            }
            if (member.getSocialType() == null){
                throw new IllegalArgumentException("이미 가입된 이메일입니다. 관리자에게 문의해주세요.");
            }
            String provider = member.getSocialType().getDisplayName();
            throw new IllegalArgumentException("이미 " + provider + "(으)로 가입된 이메일입니다. "
                    + provider + "(으)로 로그인해주세요.");
        }

        // 인증되지 않은 이메일로 가입하면, 진짜 이메일 주인이 나중에 가입하지 못하게 막을 수 있으므로 거부
        if (!emailVerified){
            throw new IllegalArgumentException("인증된 이메일이 있는 계정으로만 가입할 수 있습니다.");
        }

        Member member = Member.builder()
                .email(email)
                .name(name)
                .profileImage(picture)
                .socialType(socialType)
                .socialId(socialId)
                .build();
        memberRepository.save(member);
        return member;
    }

    @Transactional(readOnly = true)
    public MemberResDto myInfo(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new EntityNotFoundException("member is not found"));
        return MemberResDto.fromEntity(member);
    }

    public void delete(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("존재하지 않는 회원입니다."));
        List<FridgeMember> fridgeMembers = fridgeMemberRepository.findAllByMember(member);
        for(FridgeMember fridgeMember : fridgeMembers){
            if (fridgeMember.getType().equals(Type.MANAGER)){
                throw new IllegalArgumentException("관리자인 냉장고가 있습니다. 먼저 권한을 이전해주세요");
            }
        }
        memberRepository.delete(member);
    }

    public Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
    }

    public List<MyPostResDto> myposts(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new EntityNotFoundException("회원을 찾을 수 없습니다."));

        List<Post> memberPost = postRepository.findAllByMemberId(member.getId());

        return memberPost.stream()
                .map(post -> MyPostResDto.fromEntity(post, Long.valueOf(post.getViewCount())))
                .collect(Collectors.toList());
    }

    public MyStatsDto mystats(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new EntityNotFoundException("회원을 찾을 수 없습니다."));

        Long postCount = postRepository.countByMemberId(member.getId());
        Long commentCount = postCommentRepository.countByMemberId(member.getId());
        Long likeCount = postLikeRepository.countByPostMemberId(member.getId());

        return MyStatsDto.builder()
                .postCount(postCount)
                .commentCount(commentCount)
                .likeCount(likeCount)
                .build();
    }
    public MemberResDto updateName(UpdateNameDto updateNameDto){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new EntityNotFoundException("회원을 찾을 수 없습니다."));
        member.updateName(updateNameDto.getName());
        memberRepository.save(member);
        return MemberResDto.fromEntity(member);
    }

    public MemberResDto updateMyProfileImage(UpdateProfielImageDto updateProfielImageDto) throws IOException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new EntityNotFoundException("회원을 찾을 수 없습니다."));

        String profileImageUrl = s3Uploader.upload(updateProfielImageDto.getNewProfileImage());

        member.updateProfileImage(profileImageUrl);

        memberRepository.save(member);
        return MemberResDto.fromEntity(member);
    }

    public List<NotificationSettingDto> mySettings(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new EntityNotFoundException("가입되지 않은 이메일 입니다."));
        List<NotificationSetting> settings = notificationSettingRepository.findAllByMember(member);
        List<NotificationSettingDto> mySetting = settings.stream()
                .map(s -> new NotificationSettingDto(
                        s.getNotificationSettingType().name(),
                        s.isActive())
                ).collect(Collectors.toList());
        return mySetting;
    }
}
