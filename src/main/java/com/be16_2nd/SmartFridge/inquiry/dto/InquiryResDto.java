package com.be16_2nd.SmartFridge.inquiry.dto;

import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import com.be16_2nd.SmartFridge.inquiry.domain.InquiryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

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

    public static InquiryResDto fromEntity(Inquiry inquiry){
        return InquiryResDto.builder()
                .inquiryId(inquiry.getInquiryId())
                .inquiryType(inquiry.getInquiryType())
                .title(inquiry.getTitle())
                .contents(inquiry.getContents())
                .memberId(inquiry.getMember().getId())
                .build();
    }
}