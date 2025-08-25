package com.be16_2nd.SmartFridge.inquiry.dto;

import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import com.be16_2nd.SmartFridge.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class InquiryCreateDto {
    private String type;
    private String title;
    private String contents;


    public Inquiry toEntity(Member member) {
        return Inquiry.builder()
                .type(type)
                .title(title)
                .contents(contents)
                .member(member)
                .build();
    }
}
