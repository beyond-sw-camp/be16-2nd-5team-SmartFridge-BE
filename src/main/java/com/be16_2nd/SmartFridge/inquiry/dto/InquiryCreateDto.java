package com.be16_2nd.SmartFridge.inquiry.dto;

import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import com.be16_2nd.SmartFridge.inquiry.domain.InquiryType;
import com.be16_2nd.SmartFridge.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryCreateDto {
    private InquiryType inquiryType;
    private String title;
    private String contents;
    private List<MultipartFile> imageFiles = new ArrayList<>();


    public Inquiry toEntity(Member member) {
        return Inquiry.builder()
                .inquiryType(inquiryType)
                .title(title)
                .contents(contents)
                .member(member)
                .build();
    }
}
