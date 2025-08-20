package com.be16_2nd.SmartFridge.Post.dto;

import com.be16_2nd.SmartFridge.Post.domain.PostComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostCommentResDto {
    private Long id;
    private String writer;
    private String content;
    private LocalDateTime createdDate;

    public static PostCommentResDto fromEntity(PostComment postComment) {
        return PostCommentResDto.builder()
                .id(postComment.getId())
                .writer(postComment.getMember().getName())
                .content(postComment.getContent())
                .createdDate(postComment.getCreatedTime())
                .build();
    }
}
