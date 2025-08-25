package com.be16_2nd.SmartFridge.inquiry.dto;

import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import com.be16_2nd.SmartFridge.inquiry.domain.InquiryStatus;
import com.be16_2nd.SmartFridge.inquiry.domain.InquiryType;
import com.be16_2nd.SmartFridge.inquiryComment.dto.InquiryCommentResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryResDto {
    private Long inquiryId;
    private InquiryType inquiryType;
    private String title;
    private String contents;
    private UUID memberId;
    private Long fridgeId;
    private List<String> imageUrls;
    private InquiryStatus status; // 상태 추가
    private List<InquiryCommentResDto> comments;
    private LocalDateTime createdAt;

    public static InquiryResDto fromEntity(Inquiry inquiry) {
        return InquiryResDto.builder()
                .inquiryId(inquiry.getInquiryId())
                .inquiryType(inquiry.getInquiryType())
                .title(inquiry.getTitle())
                .contents(inquiry.getContents())
                .memberId(inquiry.getMember().getId())
                .status(inquiry.getStatus()) // 상태
                .comments(inquiry.getComments().stream()
                                .map(InquiryCommentResDto::new)
                                .collect(Collectors.toList()))
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}