package com.be16_2nd.SmartFridge.Post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostImageCreateDto {
    private List<MultipartFile> images;
}
