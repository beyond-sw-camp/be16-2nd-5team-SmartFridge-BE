package com.be16_2nd.SmartFridge.Post.controller;

import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import com.be16_2nd.SmartFridge.Post.dto.PostImageCreateDto;
import com.be16_2nd.SmartFridge.Post.service.PostImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class PostImageController {

    private final PostImageService postImageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImages(@ModelAttribute PostImageCreateDto createDto) {
        List<String> imageUrls = postImageService.uploadImages(createDto.getImages());
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(imageUrls)
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("이미지 업로드가 완료되었습니다.")
                        .build(),
                HttpStatus.CREATED);
    }
}