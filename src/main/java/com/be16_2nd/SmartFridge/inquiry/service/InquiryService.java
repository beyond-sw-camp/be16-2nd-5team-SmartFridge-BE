package com.be16_2nd.SmartFridge.inquiry.service;

import com.be16_2nd.SmartFridge.inquiry.dto.InquiryCreateDto;
import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import com.be16_2nd.SmartFridge.inquiry.dto.InquiryResDto;
import com.be16_2nd.SmartFridge.inquiry.repository.InquiryRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import com.be16_2nd.SmartFridge.member.service.MemberService;
import com.be16_2nd.SmartFridge.notification.domain.NotificationType;
import com.be16_2nd.SmartFridge.notification.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final MemberService memberService;

    public Long create(InquiryCreateDto inquiryCreateDto) {
        Member sender = memberService.getCurrentMember();
        Member receiver = memberRepository.findByEmail("admin@naver.com")
                .orElseThrow(() -> new EntityNotFoundException("등록되지 않은 관리자입니다."));
        
        Inquiry inquiry = inquiryRepository.save(inquiryCreateDto.toEntity(sender));

        // 알림 발송 + db 저장
        notificationService.create(sender, receiver, NotificationType.NEW_INQUIRY, inquiry);

        return inquiry.getInquiryId();
    }

    // 문의 목록 조회
    public List<InquiryResDto> getInquiryList() {
        Member member = memberService.getCurrentMember();
        return inquiryRepository.findByMember(member).stream().map(InquiryResDto::fromEntity)
                .collect(Collectors.toList());
    }
}
