package com.be16_2nd.SmartFridge.inquiry.dto;

import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import com.be16_2nd.SmartFridge.inquiry.domain.InquiryImage;
import com.be16_2nd.SmartFridge.inquiry.domain.InquiryStatus;
import com.be16_2nd.SmartFridge.inquiry.domain.InquiryType;
import com.be16_2nd.SmartFridge.inquiryComment.domain.InquiryComment;
import com.be16_2nd.SmartFridge.inquiryComment.dto.InquiryCommentResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
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
    private String memberEmail;
    private List<String> imageUrls;
    private InquiryStatus status; // 상태 추가
    private InquiryCommentResDto comments;
    private LocalDateTime createdTime;

    public static InquiryResDto fromEntity(Inquiry inquiry) {

        InquiryComment inquiryComment = inquiry.getComments();

        return InquiryResDto.builder()
                .inquiryId(inquiry.getInquiryId())
                .inquiryType(inquiry.getInquiryType())
                .title(inquiry.getTitle())
                .contents(inquiry.getContents())
                .memberEmail(inquiry.getMember().getEmail())
                .imageUrls(inquiry.getInquiryImages().stream()
                        .map(InquiryImage::getImageUrl)
                        .collect(Collectors.toList()))
                .status(inquiry.getStatus())
                .comments(inquiryComment != null ? new InquiryCommentResDto(inquiryComment) : null)
                .createdTime(inquiry.getCreatedTime())
                .build();
    }
}