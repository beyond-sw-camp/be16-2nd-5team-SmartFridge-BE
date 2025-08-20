package com.be16_2nd.SmartFridge.Post.dto;

import com.be16_2nd.SmartFridge.Post.domain.Post;
import com.be16_2nd.SmartFridge.Post.domain.PostComment;
import com.be16_2nd.SmartFridge.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostCommentCreateDto {
    private String content;

    public PostComment fromEntity(Post post , Member member) {
        return PostComment.builder()
                .post(post)
                .member(member)
                .content(this.content)
                .build();
    }
}
