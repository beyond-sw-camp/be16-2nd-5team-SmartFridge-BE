package com.be16_2nd.SmartFridge.Post.controller;

import com.be16_2nd.SmartFridge.Post.dto.PostCategoryResDto;
import com.be16_2nd.SmartFridge.Post.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class PostCategoryController {
    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        List<PostCategoryResDto> categories = postService.findAllCategories();

        return ResponseEntity.ok(categories);
    }

}
