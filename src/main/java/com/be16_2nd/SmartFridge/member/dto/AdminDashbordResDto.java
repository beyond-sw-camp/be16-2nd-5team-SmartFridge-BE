package com.be16_2nd.SmartFridge.member.dto;

import com.be16_2nd.SmartFridge.inquiry.domain.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminDashbordResDto {
    private Long inquiryCount;
    private Long unansweredCount;
    private Long userCount;
    private Long newNotification;
    private RecentInquires recentInquires;

    public static class RecentInquires{
        private Long id;
        private String title;
        private InquiryStatus status;
        private LocalDateTime createdTime;
    }
}
