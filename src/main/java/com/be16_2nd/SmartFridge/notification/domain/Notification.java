package com.be16_2nd.SmartFridge.notification.domain;

import com.be16_2nd.SmartFridge.Post.domain.Post;
import com.be16_2nd.SmartFridge.common.domain.BaseTimeEntity;
import com.be16_2nd.SmartFridge.food.domain.Food;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import com.be16_2nd.SmartFridge.inquiryComment.domain.InquiryComment;
import com.be16_2nd.SmartFridge.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fridge_id")
    private Fridge fridge;

    @Column(nullable = false)
    private String content;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    private Food food;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id")
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiryComment_id")
    private InquiryComment inquiryComment;


    public static Notification fromInquiry(Member sender, Member receiver, Inquiry inquiry) {
        String content = sender.getName() + "님이 새로운 문의를 등록했습니다.";
        return Notification.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .notificationType(NotificationType.NEW_INQUIRY)
                .inquiry(inquiry)
                .isRead(false)
                .build();
    }

    public static Notification fromInquiryComment(Member sender, Member receiver, InquiryComment inquiryComment) {
        String content = NotificationType.ADMIN_REPLY.getDescription();
        return Notification.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .inquiry(inquiryComment.getInquiry())
                .notificationType(NotificationType.ADMIN_REPLY)
                .inquiryComment(inquiryComment)
                .isRead(false)
                .build();
    }
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    private TargetType targetType;

    @Column(name = "target_id")
    private Long targetId;
}
