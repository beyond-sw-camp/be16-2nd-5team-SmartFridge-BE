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

/*
 * 채팅방 (1대1 관리자 채팅 / 공동구매 채팅 공통)
 * - 공동구매 채팅방의 게시글 정보는 PurchasePost에 분리
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
@Table(indexes = @Index(name = "idx_chat_room_fridge_type", columnList = "fridge_id, type"))
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fridge_id", nullable = false)
    private Fridge fridge;

    private LocalDateTime lastMessageAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatParticipant> participants = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    // 공동구매 채팅방일 때만 존재
    @OneToOne(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PurchasePost purchasePost;

    public void updateLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public void attachPurchasePost(PurchasePost purchasePost) {
        this.purchasePost = purchasePost;
    }

    public ChatParticipant addParticipant(Member member, ParticipantRole role) {
        ChatParticipant participant = ChatParticipant.builder()
                .chatRoom(this)
                .member(member)
                .role(role)
                .build();
        this.participants.add(participant);
        return participant;
    }
}
