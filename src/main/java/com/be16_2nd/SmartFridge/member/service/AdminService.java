package com.be16_2nd.SmartFridge.member.service;

import com.be16_2nd.SmartFridge.fridge.domain.Type;
import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import com.be16_2nd.SmartFridge.inquiry.domain.InquiryStatus;
import com.be16_2nd.SmartFridge.inquiry.dto.InquiryResDto;
import com.be16_2nd.SmartFridge.inquiry.repository.InquiryRepository;
import com.be16_2nd.SmartFridge.inquiryComment.domain.InquiryComment;
import com.be16_2nd.SmartFridge.inquiryComment.dto.InquiryCommentCreateDto;
import com.be16_2nd.SmartFridge.inquiryComment.dto.InquiryCommentResDto;
import com.be16_2nd.SmartFridge.inquiryComment.repository.InquiryCommentRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.dto.MemberResDto;
import com.be16_2nd.SmartFridge.member.dto.AdminDashbordResDto;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import com.be16_2nd.SmartFridge.notification.domain.NotificationType;
import com.be16_2nd.SmartFridge.notification.dto.NotificationResDto;
import com.be16_2nd.SmartFridge.notification.repository.NotificationRepository;
import com.be16_2nd.SmartFridge.notification.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final InquiryRepository inquiryRepository;
    private final NotificationRepository notificationRepository;
    private final InquiryCommentRepository inquiryCommentRepository;
    private final NotificationService notificationService;


    @Transactional(readOnly = true)
    public List<MemberResDto> findAllMember(){
        return memberRepository.findAll().stream()
                .map(m->MemberResDto.fromEntity(m)).collect(Collectors.toList());
    }

    public Member deleteMember(String memberEmail){
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(()->new EntityNotFoundException("Member Not Found"));
        memberRepository.delete(member);
        return member;
    }

    public AdminDashbordResDto dashbord(){
        Member member = memberRepository.findByEmail("admin@naver.com")
                .orElseThrow(()->new EntityNotFoundException("ONLY ADMIN ALLOWED"));

        return AdminDashbordResDto.builder()
                .inquiryCount(inquiryRepository.count())
                .unansweredCount(inquiryRepository.countAllByStatus(InquiryStatus.PENDING))
                .userCount(memberRepository.count())
                .newNotification(notificationRepository.countByReceiverAndIsReadFalse(member))
                .build();
    }

    @Transactional(readOnly = true)
    public List<InquiryResDto> findAllInquiry(){
        return inquiryRepository.findAll().stream()
                .map(i-> InquiryResDto.fromEntity(i)).collect(Collectors.toList());
    }

    public InquiryCommentResDto replyInquiry(InquiryCommentCreateDto inquiryCommentCreateDto, Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new EntityNotFoundException("등록되지 않은 문의입니다."));
        Member sender = memberRepository.findByEmail("admin@naver.com")
                .orElseThrow(() -> new EntityNotFoundException("등록되지 않은 관리자입니다."));
        String email = inquiry.getMember().getEmail();
        Member receiver = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("등록되지 않은 회원입니다."));
        InquiryComment inquiryComment = inquiryCommentRepository.save(inquiryCommentCreateDto.toEntity(inquiryCommentCreateDto, inquiry));
        inquiry.setStatus(InquiryStatus.COMPLETED);

        // 알림 발송 + db 저장
        notificationService.create(sender, receiver, NotificationType.ADMIN_REPLY, inquiryComment);
        return InquiryCommentResDto.builder()
                .id(inquiryComment.getCommentId())
                .contents(inquiryComment.getCommentContents())
                .inquiryId(inquiryId)
                .build();
    }

    public InquiryResDto inquiryDetail(Long inquiryId){
        Inquiry inquiry = inquiryRepository.findAllByInquiryId(inquiryId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 문의글입니다."));
        return InquiryResDto.fromEntity(inquiry);
    }

    public MemberResDto memberDetail(String memberEmail){
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 회원입니다"));
        return MemberResDto.fromEntity(member);
    }

    public List<NotificationResDto> findAllNotification(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new EntityNotFoundException("ONLY ADMIN ALLOWED"));
        return notificationRepository.findByReceiverOrderByIsReadAscCreatedTimeDesc(member)
                .stream().map(notification -> NotificationResDto.fromEntity(notification, Type.ADMIN))
                .collect(Collectors.toList());
    }
    public List<NotificationResDto> findAllReadNotification(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new EntityNotFoundException("ONLY ADMIN ALLOWED"));
        return notificationRepository.findByReceiverAndIsReadTrueOrderByCreatedTimeDesc(member)
                .stream().map(notification -> NotificationResDto.fromEntity(notification, Type.ADMIN))
                .collect(Collectors.toList());
    }
    public List<NotificationResDto> findAllUnreadNotification(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new EntityNotFoundException("ONLY ADMIN ALLOWED"));
        return notificationRepository.findByReceiverAndIsReadFalseOrderByCreatedTimeDesc(member)
                .stream().map(notification -> NotificationResDto.fromEntity(notification, Type.ADMIN))
                .collect(Collectors.toList());
    }
}
