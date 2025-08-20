package com.be16_2nd.SmartFridge.inquiryComment.service;

import com.be16_2nd.SmartFridge.notification.service.NotificationPublisher;
import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import com.be16_2nd.SmartFridge.inquiry.repository.InquiryRepository;
import com.be16_2nd.SmartFridge.inquiryComment.domain.InquiryComment;
import com.be16_2nd.SmartFridge.inquiryComment.dto.InquiryCommentCreateDto;
import com.be16_2nd.SmartFridge.inquiryComment.dto.InquiryCommentResDto;
import com.be16_2nd.SmartFridge.inquiryComment.repository.InquiryCommentRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import com.be16_2nd.SmartFridge.notification.domain.Notification;
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
        Notification notification = Notification.fromInquiryComment(sender, receiver, inquiryComment);
        notificationService.create(notification);
        return new InquiryCommentResDto(inquiryComment);
    }
}
