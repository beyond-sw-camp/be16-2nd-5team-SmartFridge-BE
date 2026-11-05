package com.be16_2nd.SmartFridge.inquiryComment.domain;

import com.be16_2nd.SmartFridge.common.domain.BaseTimeEntity;
import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_inquiry_comment_inquiry_id", columnNames = "inquiry_id"))
public class InquiryComment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;
    private String commentContents;

    // 문의 1건당 답변 1개 (uk_inquiry_comment_inquiry_id)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id")
    private Inquiry inquiry;

    public void updateCommentContents(String commentContents){
        this.commentContents=commentContents;
    }
}
