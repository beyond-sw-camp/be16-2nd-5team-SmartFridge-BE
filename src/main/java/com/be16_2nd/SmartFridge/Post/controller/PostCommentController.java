package com.be16_2nd.SmartFridge.Post.controller;

import com.be16_2nd.SmartFridge.common.dto.CommonDto;
import com.be16_2nd.SmartFridge.Post.dto.PostCommentCreateDto;
import com.be16_2nd.SmartFridge.Post.dto.PostCommentResDto;
import com.be16_2nd.SmartFridge.Post.dto.PostCommentUpdateDto;
import com.be16_2nd.SmartFridge.Post.service.PostCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fridge/{fridgeId}/post/{postId}/comment")
public class PostCommentController {

    private final PostCommentService postCommentService;

    @PostMapping("create")
    public ResponseEntity<?> create(@PathVariable Long fridgeId, @PathVariable Long postId,
                                    @Valid @RequestBody PostCommentCreateDto dto) {
        Long id = postCommentService.createPostComment(fridgeId, postId, dto);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(id)
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("댓글 등록 완료")
                        .build(),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(@PathVariable Long fridgeId, @PathVariable Long postId, Pageable pageable) {
        Page<PostCommentResDto> comments = postCommentService.list(fridgeId, postId, pageable);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(comments)
                        .status_code(HttpStatus.OK.value())
                        .status_message("댓글 목록 조회 완료")
                        .build(),
                HttpStatus.OK
        );
    }

    @PatchMapping("/update/{commentId}")
    public ResponseEntity<?> update(@PathVariable Long fridgeId, @PathVariable Long postId,
                                    @PathVariable Long commentId, @Valid @RequestBody PostCommentUpdateDto dto) {
        Long id = postCommentService.update(fridgeId, postId, commentId, dto.getContent());
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(id)
                        .status_code(HttpStatus.OK.value())
                        .status_message("댓글 수정 완료")
                        .build(),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<?> delete(@PathVariable Long fridgeId, @PathVariable Long postId,
                                    @PathVariable Long commentId) {
        Long id = postCommentService.delete(fridgeId, postId, commentId);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(id)
                        .status_code(HttpStatus.OK.value())
                        .status_message("댓글 삭제 완료")
                        .build(),
                HttpStatus.OK
        );
    }
}