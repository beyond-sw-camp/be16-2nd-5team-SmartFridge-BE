package com.be16_2nd.SmartFridge.inquiry.service;

import com.be16_2nd.SmartFridge.common.service.S3Uploader;
import com.be16_2nd.SmartFridge.inquiry.domain.InquiryImage;
import com.be16_2nd.SmartFridge.inquiry.dto.InquiryCreateDto;
import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import com.be16_2nd.SmartFridge.inquiry.dto.InquiryResDto;
import com.be16_2nd.SmartFridge.inquiry.dto.InquiryUpdateDto;
import com.be16_2nd.SmartFridge.inquiry.repository.InquiryRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import com.be16_2nd.SmartFridge.member.service.MemberService;
import com.be16_2nd.SmartFridge.notification.domain.NotificationType;
import com.be16_2nd.SmartFridge.notification.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final S3Uploader s3Uploader;

    public Long create(InquiryCreateDto inquiryCreateDto) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member sender = memberRepository.findByEmail(email)
                .orElseThrow(()-> new EntityNotFoundException("등록되지 않은 사용자입니다."));
        Member receiver = memberRepository.findByEmail("admin@naver.com")
                .orElseThrow(() -> new EntityNotFoundException("등록되지 않은 관리자입니다."));

        Inquiry inquiry = inquiryCreateDto.toEntity(sender);

        for (MultipartFile image : inquiryCreateDto.getImageFiles()){
            String url = s3Uploader.upload(image);
            InquiryImage inquiryImage = InquiryImage.builder()
                    .imageUrl(url)
                    .inquiry(inquiry)
                    .build();
            inquiry.getInquiryImages().add(inquiryImage);
        }
        inquiryRepository.save(inquiry);
        // 알림 발송 + db 저장
        notificationService.create(sender, receiver, NotificationType.NEW_INQUIRY, inquiry);

        return inquiry.getInquiryId();
    }


    @Transactional(readOnly = true)
    public List<InquiryResDto> findMyInquiry(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("존재하지 않는 사용자 입니다"));
        return inquiryRepository.findAllByMember(member).stream()
                .map(i-> InquiryResDto.fromEntity(i)).collect(Collectors.toList());
    }

    public Long updateInquiry(Long inquiryId, InquiryUpdateDto inquiryUpdateDto){
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("문의 없음"));

        if (inquiryUpdateDto.getTitle() != null)
            inquiry.setTitle(inquiryUpdateDto.getTitle());
        if (inquiryUpdateDto.getContents() != null)
            inquiry.setContents(inquiryUpdateDto.getContents());
        if (inquiryUpdateDto.getInquiryType() != null)
            inquiry.setInquiryType(inquiryUpdateDto.getInquiryType());

        if (inquiry.getInquiryImages() != null && !inquiry.getInquiryImages().isEmpty()) {
            for (InquiryImage inquiryImage : inquiry.getInquiryImages()) {
                s3Uploader.delete(inquiryImage.getImageUrl());
            }
            inquiry.getInquiryImages().clear();
        }

        if (inquiryUpdateDto.getImageFiles() != null) {
            for (MultipartFile newImage : inquiryUpdateDto.getImageFiles()) {
                String url = s3Uploader.upload(newImage);
                InquiryImage inquiryImage = InquiryImage.builder()
                        .imageUrl(url)
                        .inquiry(inquiry)
                        .build();
                inquiry.getInquiryImages().add(inquiryImage);
            }
        }


        return inquiry.getInquiryId();
    }

    public Long deleteInquiry(Long inquiryId){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("존재하지 않는 사용자입니다."));
        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElseThrow(()->new EntityNotFoundException("존재하지 않는 글입니다."));
        inquiryRepository.delete(inquiry);
    return inquiryId;
    }
}
