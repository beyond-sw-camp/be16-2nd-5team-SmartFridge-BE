package com.be16_2nd.SmartFridge.chat.domain;

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
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_chat_id")
    private ManagerChatRoom managerChatRoom;

    @Enumerated(EnumType.STRING)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_chat_id")
    private PurchaseChatRoom purchaseChatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdTime;

    private String contents;

    private boolean isDeleted;

    @Enumerated(EnumType.STRING)
    private ChatRoomType chatRoomType;

    @Builder.Default
    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IsRead> isReads = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessageImage> chatMessageImages = new ArrayList<>();
}
