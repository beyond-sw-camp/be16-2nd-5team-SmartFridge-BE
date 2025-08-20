package com.be16_2nd.SmartFridge.common.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PostStatsService {
    private final RedisTemplate<String, Long> viewTemplate;
    private final RedisTemplate<String, String> likeTemplate;

    public PostStatsService(@Qualifier("postViewStats") RedisTemplate<String, Long> viewTemplate,
                            @Qualifier("postLikeStats") RedisTemplate<String, String> likeTemplate) {
        this.viewTemplate = viewTemplate;
        this.likeTemplate = likeTemplate;
    }

    public void incrementViewCount(Long postId) {
        String key = postId + ":views";
        viewTemplate.opsForValue().increment(key);
    }

    public Long getViewCount(Long postId) {
        String key = postId + ":views";
        Long viewCount = viewTemplate.opsForValue().get(key);
        return viewCount == null ? 0L : viewCount;
    }

    public void addLike(Long postId, UUID memberId) {
        String key = postId + ":likes";
        likeTemplate.opsForSet().add(key, String.valueOf(memberId));
    }

    public void removeLike(Long postId, UUID memberId) {
        String key = postId + ":likes";
        likeTemplate.opsForSet().remove(key, String.valueOf(memberId));
    }

    public Long getLikeCount(Long postId) {
        String key = postId + ":likes";
        return likeTemplate.opsForSet().size(key);
    }

    public boolean isLiked(Long postId, UUID memberId) {
        String key = postId + ":likes";
        return Boolean.TRUE.equals(likeTemplate.opsForSet().isMember(key, String.valueOf(memberId)));
    }
}