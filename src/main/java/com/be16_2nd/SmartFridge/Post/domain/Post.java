package com.be16_2nd.SmartFridge.Post.domain;

import com.be16_2nd.SmartFridge.Post.dto.PostUpdateDto;
import com.be16_2nd.SmartFridge.common.domain.BaseTimeEntity;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String content;
    @Column(nullable = false)
    private int viewCount = 0;
    @Column(nullable = false)
    private int commentCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private PostCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fridge_id")
    private Fridge fridge;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    public void updatePost(PostUpdateDto postUpdateDto, PostCategory postCateogory) {
        this.title = postUpdateDto.getTitle();
        this.content = postUpdateDto.getContent();
        this.category = postCateogory;
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
}
