package com.be16_2nd.SmartFridge.chat.domain;

import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
public class PurchaseChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fridge_id", nullable = false)
    private Fridge fridge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Member creator;

    @Column(nullable = false)
    private String title;

    private String contents;

    @Column(nullable = false)
    private Integer currentParticipants;
    @Column(nullable = false)
    private Integer maxParticipants;

    @Column(nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private JoinStatus joinStatus = JoinStatus.OPEN;

    private String productUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdTime;

    private LocalDateTime lastMessageAt;
    
    // 양방향매핑
    @OneToMany(mappedBy = "purchaseChatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatParticipant> participants = new ArrayList<>();

    // 현재 참여자 수 변경
    public void updateCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    // 구매 모집 상태 변경
    public void updateJoinStatus(JoinStatus joinStatus) {
        this.joinStatus = joinStatus;
    }

    public void updateIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void updateLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}
