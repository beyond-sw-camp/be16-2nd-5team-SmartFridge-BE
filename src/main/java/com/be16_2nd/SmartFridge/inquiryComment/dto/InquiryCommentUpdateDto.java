package com.be16_2nd.SmartFridge.inquiryComment.dto;

import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import com.be16_2nd.SmartFridge.inquiryComment.domain.InquiryComment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryCommentUpdateDto {
    private String newCommentContents;

    public InquiryComment toEntity(InquiryCommentUpdateDto inquiryCommentUpdateDto, Inquiry inquiry) {
        return InquiryComment.builder()
                .commentContents(inquiryCommentUpdateDto.getNewCommentContents())
                .inquiry(inquiry).build();
    }
}
