package com.be16_2nd.SmartFridge.Post.service;

import com.be16_2nd.SmartFridge.common.service.FridgeAccessValidator;
import com.be16_2nd.SmartFridge.common.service.FridgeAccessValidator.FridgeContext;
import com.be16_2nd.SmartFridge.Post.domain.Post;
import com.be16_2nd.SmartFridge.Post.domain.PostComment;
import com.be16_2nd.SmartFridge.Post.dto.PostCommentCreateDto;
import com.be16_2nd.SmartFridge.Post.dto.PostCommentResDto;
import com.be16_2nd.SmartFridge.Post.repository.PostCommentRepository;
import com.be16_2nd.SmartFridge.Post.repository.PostRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final FridgeAccessValidator fridgeAccessValidator;

    public Long createPostComment(Long fridgeId, Long postId, PostCommentCreateDto createDto) {
        FridgeContext context = fridgeAccessValidator.validate(fridgeId);
        Member member = context.member();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("없는 게시글입니다."));

        if (!post.getFridge().getId().equals(fridgeId)) {
            throw new AccessDeniedException("해당 냉장고의 게시글이 아닙니다.");
        }

        PostComment postComment = createDto.fromEntity(post, member);

        postCommentRepository.save(postComment);

        post.increaseCommentCount();

        return postComment.getId();
    }

    @Transactional(readOnly = true)
    public Page<PostCommentResDto> list(Long fridgeId, Long postId, Pageable pageable) {
        fridgeAccessValidator.validate(fridgeId);

        return postCommentRepository.findByPostId(postId, pageable)
                .map(PostCommentResDto::fromEntity);
    }

    public Long update(Long fridgeId, Long postId, Long commentId, String newContent) {
        FridgeContext context = fridgeAccessValidator.validate(fridgeId);
        Member member = context.member();

        PostComment postComment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("없는 댓글입니다."));

        if (!postComment.getPost().getId().equals(postId) || !postComment.getPost().getFridge().getId().equals(fridgeId)) {
            throw new AccessDeniedException("잘못된 접근입니다.");
        }

        if (!postComment.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("댓글 수정 권한이 없습니다.");
        }

        postComment.update(newContent);

        return postComment.getId();
    }

    public Long delete(Long fridgeId, Long postId, Long commentId) {
        FridgeContext context = fridgeAccessValidator.validate(fridgeId);
        Member member = context.member();

        PostComment postComment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("없는 댓글입니다."));

        if (!postComment.getPost().getId().equals(postId) || !postComment.getPost().getFridge().getId().equals(fridgeId)) {
            throw new AccessDeniedException("잘못된 접근입니다.");
        }

        if (!postComment.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }

        Post post = postComment.getPost();
        post.decreaseCommentCount();

        postCommentRepository.delete(postComment);

        return commentId;
    }
}