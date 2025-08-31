package com.be16_2nd.SmartFridge.notification.dto;

import com.be16_2nd.SmartFridge.notification.domain.Notification;
import com.be16_2nd.SmartFridge.notification.domain.NotificationType;
import com.be16_2nd.SmartFridge.notification.domain.TargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationBadgeResDto {
    private Long id;
    private String content;
    private boolean isRead;
    private NotificationType type;
    private LocalDateTime createdAt;
    private TargetType targetType;
    private Long targetId;

    public static NotificationBadgeResDto fromEntity(Notification notification) {
        return NotificationBadgeResDto.builder()
                .id(notification.getId())
                .content(notification.getContent())
                .isRead(notification.isRead())
                .type(notification.getNotificationType())
                .targetType(notification.getTargetType())
                .targetId(notification.getTargetId())
                .createdAt(notification.getCreatedTime())
                .build();
    }
}