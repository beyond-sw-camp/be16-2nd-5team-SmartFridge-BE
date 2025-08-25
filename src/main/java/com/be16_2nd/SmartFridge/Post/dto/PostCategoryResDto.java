package com.be16_2nd.SmartFridge.Post.dto;

import com.be16_2nd.SmartFridge.Post.domain.PostCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostCategoryResDto {
    private Long id;
    private String category;

    public static PostCategoryResDto fromEntity(PostCategory postCategory) {
        return PostCategoryResDto.builder().id(postCategory.getId()).category(postCategory.getCategory()).build();
    }
}
