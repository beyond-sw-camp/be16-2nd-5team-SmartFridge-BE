package com.be16_2nd.SmartFridge.inquiryComment.dto;

import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import com.be16_2nd.SmartFridge.inquiryComment.domain.InquiryComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InquiryCommentResDto {
    private Long id;
    private String contents;
    private Long inquiryId;

    public InquiryCommentResDto(InquiryComment inquiryComment) {
        this.id = inquiryComment.getCommentId();
        this.contents = inquiryComment.getCommentContents();
        this.inquiryId = inquiryComment.getInquiry().getInquiryId();
    }
}
