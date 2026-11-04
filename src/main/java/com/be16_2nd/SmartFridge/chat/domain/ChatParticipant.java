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
 * 채팅방 참여자
 * - lastReadMessageId : 마지막으로 읽은 메시지 id (읽음 커서). 이 값보다 큰 id의 메시지가 안 읽은 메시지
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_chat_participant_room_member", columnNames = {"chat_room_id", "member_id"}))
public class ChatParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ParticipantRole role = ParticipantRole.MEMBER;

    @Column(nullable = false)
    @Builder.Default
    private Long lastReadMessageId = 0L;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    public void readUpTo(Long messageId) {
        if (messageId != null && messageId > this.lastReadMessageId) {
            this.lastReadMessageId = messageId;
        }
    }
}
