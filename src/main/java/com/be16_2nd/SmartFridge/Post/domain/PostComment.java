package com.be16_2nd.SmartFridge.Post.domain;

import com.be16_2nd.SmartFridge.common.domain.BaseTimeEntity;
import com.be16_2nd.SmartFridge.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class PostComment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public void update(String newContent) {
        this.content = newContent;
    }
}
