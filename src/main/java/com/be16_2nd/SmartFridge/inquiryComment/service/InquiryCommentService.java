package com.be16_2nd.SmartFridge.inquiryComment.service;

import com.be16_2nd.SmartFridge.notification.domain.NotificationType;
import com.be16_2nd.SmartFridge.notification.service.NotificationPublisher;
import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import com.be16_2nd.SmartFridge.inquiry.repository.InquiryRepository;
import com.be16_2nd.SmartFridge.inquiryComment.domain.InquiryComment;
import com.be16_2nd.SmartFridge.inquiryComment.dto.InquiryCommentCreateDto;
import com.be16_2nd.SmartFridge.inquiryComment.dto.InquiryCommentResDto;
import com.be16_2nd.SmartFridge.inquiryComment.repository.InquiryCommentRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import com.be16_2nd.SmartFridge.notification.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryCommentService {

    private final InquiryCommentRepository inquiryCommentRepository;
    private final InquiryRepository inquiryRepository;
    private final NotificationPublisher notificationPublisher;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    public InquiryCommentResDto create(InquiryCommentCreateDto inquiryCommentCreateDto, Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new EntityNotFoundException("등록되지 않은 문의입니다."));
        Member sender = memberRepository.findByEmail("admin@naver.com")
                .orElseThrow(() -> new EntityNotFoundException("등록되지 않은 관리자입니다."));
        String email = inquiry.getMember().getEmail();
        Member receiver = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("등록되지 않은 회원입니다."));
        InquiryComment inquiryComment = inquiryCommentRepository.save(inquiryCommentCreateDto.toEntity(inquiryCommentCreateDto, inquiry));

        // 알림 발송 + db 저장
        notificationService.create(sender, receiver, NotificationType.ADMIN_REPLY, inquiryComment);
        return new InquiryCommentResDto(inquiryComment);
    }
}
