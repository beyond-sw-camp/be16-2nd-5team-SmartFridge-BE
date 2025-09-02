package com.be16_2nd.SmartFridge.member.service;

import com.be16_2nd.SmartFridge.Post.domain.Post;
import com.be16_2nd.SmartFridge.Post.repository.PostCommentRepository;
import com.be16_2nd.SmartFridge.Post.repository.PostLikeRepository;
import com.be16_2nd.SmartFridge.Post.repository.PostRepository;
import com.be16_2nd.SmartFridge.common.service.S3Uploader;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import com.be16_2nd.SmartFridge.member.dto.*;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
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

    public Member save(MemberCreateDto memberCreateDto){
        if (memberRepository.findByEmail(memberCreateDto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다.");
        }
        String password = memberCreateDto.getPassword();
        String passwordConfirm = memberCreateDto.getPasswordConfirm();

        if (!password.equals(passwordConfirm)){
            throw new IllegalArgumentException("비밀번호를 확인해 주세요");
        }
        Member member = memberRepository.save(memberCreateDto.toEntity(passwordEncoder.encode(memberCreateDto.getPassword())));
        return member;
    }

    public Member doLogin(LoginReqDto loginReqDto){
        Optional<Member> optionalMember = memberRepository.findByEmail(loginReqDto.getEmail());
        boolean check = true;
        if (!optionalMember.isPresent()){
            check = false;
        } else {
            if (!passwordEncoder.matches(loginReqDto.getPassword(), optionalMember.get().getPassword())){
                check = false;
            }
        }
        if (!check){
            throw new IllegalArgumentException("id, email또는 비밀번호가 일치하지 않습니다.");
        }
        return optionalMember.get();
    }

    public Member getMemberBySocialId(String socialId){
        Member member = memberRepository.findBySocialId(socialId).orElse(null);
        return member;
    }

    public Member createOauth(String socialId, String email, String name, String picture, SocialType socialType){
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
        memberRepository.delete(member);
    }

    public Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
    }

    public Member updatePw(UpdatePwDto updatePwDto){
        Member member = memberRepository.findByEmail(updatePwDto.getEmail())
                .orElseThrow(()->new EntityNotFoundException("가입되지 않은 이메일입니다."));
        String password = updatePwDto.getNewPassword();
        String passwordConfirm = updatePwDto.getNewPasswordConfirm();

        if (!password.equals(passwordConfirm)){
            throw new IllegalArgumentException("비밀번호를 확인해 주세요");
        }

        member.updatePw(passwordEncoder.encode(password));
        memberRepository.save(member);
        return member;
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
}
