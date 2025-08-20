package com.be16_2nd.SmartFridge.Post.controller;

import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import com.be16_2nd.SmartFridge.Post.dto.PostCreateDto;
import com.be16_2nd.SmartFridge.Post.dto.PostResDto;
import com.be16_2nd.SmartFridge.Post.dto.PostSearchDto;
import com.be16_2nd.SmartFridge.Post.dto.PostUpdateDto;
import com.be16_2nd.SmartFridge.Post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fridge/{fridgeId}/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@PathVariable("fridgeId") Long fridgeId, @ModelAttribute PostCreateDto createDto) {
        Long postId = postService.createPost(fridgeId, createDto);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(postId)
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("게시글 등록이 완료되었습니다.")
                        .build(),
                HttpStatus.CREATED);
    }

    @PutMapping("/{postId}/update")
    public ResponseEntity<?> update(@PathVariable("fridgeId") Long fridgeId, @PathVariable("postId") Long postId, @ModelAttribute PostUpdateDto updateDto) {
        Long updatedPostId = postService.updatePost(fridgeId, postId, updateDto);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(updatedPostId)
                        .status_code(HttpStatus.OK.value())
                        .status_message("게시글 수정이 완료되었습니다.")
                        .build(),
                HttpStatus.OK);
    }

    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<?> delete(@PathVariable("fridgeId") Long fridgeId, @PathVariable("postId") Long postId) {
        postService.deletePost(fridgeId, postId);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .status_code(HttpStatus.OK.value())
                        .status_message("게시글 삭제가 완료되었습니다.")
                        .build(),
                HttpStatus.OK);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> postDetail(@PathVariable("fridgeId") Long fridgeId, @PathVariable("postId") Long postId) {
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(postService.findById(fridgeId, postId))
                        .status_code(HttpStatus.OK.value())
                        .status_message("게시글 상세 조회 완료")
                        .build(),
                HttpStatus.OK);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> postLike(@PathVariable("fridgeId") Long fridgeId,@PathVariable("postId") Long postId){
        postService.addLike(fridgeId, postId);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .status_code(HttpStatus.OK.value())
                        .status_message("좋아요 처리가 완료되었습니다.")
                        .build(),
                HttpStatus.OK);
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<?> removeLike(@PathVariable("fridgeId") Long fridgeId,@PathVariable("postId") Long postId){
        postService.removeLike(fridgeId, postId);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .status_code(HttpStatus.OK.value())
                        .status_message("좋아요 취소가 완료되었습니다.")
                        .build(),
                HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<?> postList(@PathVariable("fridgeId") Long fridgeId, Pageable pageable,
                                         @ModelAttribute PostSearchDto searchDto, @RequestParam(required = false, defaultValue = "전체") String categoryName) {
        Page<PostResDto> postPage = postService.findByPosts(fridgeId, pageable, searchDto, categoryName);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(postPage)
                        .status_code(HttpStatus.OK.value())
                        .status_message("게시글 목록 조회 완료")
                        .build(),
                HttpStatus.OK);
    }
}