package com.be16_2nd.SmartFridge.notification.domain;

import com.be16_2nd.SmartFridge.Post.domain.Post;
import com.be16_2nd.SmartFridge.Post.domain.PostComment;
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
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Member receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fridge_id")
    private Fridge fridge;

    @Column(nullable = false)
    private String content;

    @Column(name = "is_read")
    private boolean isRead;

    @Enumerated(EnumType.STRING)
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
                .fridge(inquiry.getFridge())
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

    // 게시글 공지사항 등록 알림
    public static Notification fromAnnounce(Member sender, Member receiver, Post post) {
        String content = "'" + post.getFridge().getFridgeName() + "' 냉장고에 새로운 공지사항이 등록되었습니다.";

        return Notification.builder()
                .sender(sender)
                .receiver(receiver)
                .fridge(post.getFridge())
                .content(content)
                .notificationType(NotificationType.NEW_ANNOUNCEMENT)
                .post(post) // 알림과 공지사항 게시글을 연결
                .isRead(false)
                .build();
    }

    // 게시글 댓글 알림
    public static Notification fromComment(Member sender, Member receiver, PostComment postComment) {

        Post post = postComment.getPost();

        String content = sender.getName() + "님이 '" + post.getTitle() + "' 게시글에 댓글을 남겼습니다.";

        return Notification.builder()
                .sender(sender)
                .receiver(receiver)
                .fridge(post.getFridge())
                .content(content)
                .notificationType(NotificationType.NEW_COMMENT)
                .post(postComment.getPost())
                .isRead(false)
                .build();
    }

    // 식자재 등록 알림
    public static Notification fromFood(Member sender, Member receiver, Food food) {

        String content = sender.getName() + "님이 '" + food.getFridge().getFridgeName() + "' 냉장고에 '" + food.getName() + "'을(를) 등록했습니다.";

        return Notification.builder()
                .sender(sender)
                .receiver(receiver)
                .fridge(food.getFridge())
                .content(content)
                .notificationType(NotificationType.NEW_FOOD)
                .food(food)
                .isRead(false)
                .build();
    }

    // 식자재 유통기한 알림
    public static Notification fromExpiration(Member receiver, Food food, String daysLeftMessage) {

        String content = "'" + food.getFridge().getFridgeName() + "'의 '" + food.getName() +
                "' 유통기한이 " + daysLeftMessage;

        return Notification.builder()
                .sender(null)           //  sender가 시스템
                .receiver(receiver)
                .fridge(food.getFridge())
                .content(content)
                .notificationType(NotificationType.EXPIRATION_IMMINENT)
                .food(food)
                .isRead(false)
                .build();
    }


}
