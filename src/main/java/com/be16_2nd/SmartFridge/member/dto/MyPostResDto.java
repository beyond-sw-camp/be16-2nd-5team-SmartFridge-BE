package com.be16_2nd.SmartFridge.member.dto;

import com.be16_2nd.SmartFridge.Post.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MyPostResDto {
    private Long fridgeId;
    private String fridgeName;
    private Long id;
    private String title;
    private String writer;
    private String categoryName;
    private Long viewCount;
    private String timeAgo;

    public static MyPostResDto fromEntity(Post post, Long viewCount) {
        return MyPostResDto.builder()
                .fridgeId(post.getFridge().getId())
                .fridgeName(post.getFridge().getFridgeName())
                .id(post.getId())
                .title(post.getTitle())
                .writer(post.getMember().getName())
                .categoryName(post.getCategory().getCategory())
                .viewCount(viewCount)
                .timeAgo(calculateTimeAgo(post.getCreatedTime()))
                .build();
    }

    private static String calculateTimeAgo(LocalDateTime createdTime) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(createdTime, now);
        long hours = ChronoUnit.HOURS.between(createdTime, now);
        long days = ChronoUnit.DAYS.between(createdTime, now);

        if (minutes < 1) return "방금전";
        if (minutes < 60) return minutes + "분전";
        if (hours < 24) return hours + "시간전";
        if (days < 7) return days + "일전";
        return createdTime.format(java.time.format.DateTimeFormatter.ofPattern("MM/dd"));
    }
}
