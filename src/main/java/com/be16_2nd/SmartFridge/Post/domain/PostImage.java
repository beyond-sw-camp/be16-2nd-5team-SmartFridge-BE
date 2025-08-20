package com.be16_2nd.SmartFridge.Post.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public static PostImage toEntity(String imageUrl, Post post) {
        return PostImage.builder()
                .imageUrl(imageUrl)
                .post(post)
                .build();
    }
}
