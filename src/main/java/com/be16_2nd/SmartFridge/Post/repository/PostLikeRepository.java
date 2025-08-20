package com.be16_2nd.SmartFridge.Post.repository;

import com.be16_2nd.SmartFridge.Post.domain.Post;
import com.be16_2nd.SmartFridge.Post.domain.PostLike;
import com.be16_2nd.SmartFridge.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByMemberAndPost(Member member, Post post);
    Optional<PostLike> findByMemberAndPost(Member member, Post post);
}
