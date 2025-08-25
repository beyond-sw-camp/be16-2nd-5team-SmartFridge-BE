package com.be16_2nd.SmartFridge.Post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostCreateDto {
    private String title;
    private String content;
    private Long categoryId;
    private List<String> imageUrls;

}
