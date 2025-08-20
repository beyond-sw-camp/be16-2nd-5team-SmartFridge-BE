package com.be16_2nd.SmartFridge.chat.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
public class ChatMessageImage{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_massage_id", nullable = false)
    private ChatMessage chatMessage;

    @Column(nullable = false)
    private String imageUrl;
}
