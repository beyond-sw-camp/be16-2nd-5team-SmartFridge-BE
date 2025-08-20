package com.be16_2nd.SmartFridge.Post.repository;

import com.be16_2nd.SmartFridge.Post.domain.Post;
import com.be16_2nd.SmartFridge.Post.domain.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    Page<PostComment> findByPostId(Long postId, Pageable pageable);
}
