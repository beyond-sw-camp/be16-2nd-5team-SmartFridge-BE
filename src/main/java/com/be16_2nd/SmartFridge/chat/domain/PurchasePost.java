package com.be16_2nd.SmartFridge.chat.domain;

import com.be16_2nd.SmartFridge.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/*
 * 공동구매 게시글 (채팅방과 1:1)
 * - 냉장고는 chatRoom.fridge 로 접근
 * - 현재 참여자 수는 chatRoom.participants 로 계산
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
public class PurchasePost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false, unique = true)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Member creator;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String contents;

    private String productUrl;

    @Column(nullable = false)
    private Integer maxParticipants;

    @Column(nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private JoinStatus joinStatus = JoinStatus.OPEN;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    public int getCurrentParticipants() {
        return chatRoom.getParticipants().size();
    }

    public boolean isFull() {
        return getCurrentParticipants() >= maxParticipants;
    }

    public void updateJoinStatus(JoinStatus joinStatus) {
        this.joinStatus = joinStatus;
    }

    public void updateIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
