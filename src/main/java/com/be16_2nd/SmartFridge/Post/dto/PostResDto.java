package com.be16_2nd.SmartFridge.Post.dto;

import com.be16_2nd.SmartFridge.Post.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostResDto {
    private Long id;
    private String title;
    private String writer;
    private String categoryName;

    public static PostResDto fromEntity(Post post) {
        return PostResDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .writer(post.getMember().getName())
                .categoryName(post.getCategory().getCategory())
                .build();
    }
}