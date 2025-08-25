package com.be16_2nd.SmartFridge.inquiry.dto;

import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;

public record InquiryResDto(
        Long inquiryId,
        String type,
        String title,
        String contents,
        boolean hasAnswer
) {
    public static InquiryResDto fromEntity(Inquiry inquiry) {
        boolean hasAnswer = !inquiry.getComments().isEmpty();
        return new InquiryResDto(
                inquiry.getInquiryId(),
                inquiry.getType(),
                inquiry.getTitle(),
                inquiry.getContents(),
                hasAnswer
        );
    }
}