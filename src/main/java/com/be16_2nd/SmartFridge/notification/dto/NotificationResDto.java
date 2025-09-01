package com.be16_2nd.SmartFridge.notification.dto;

import com.be16_2nd.SmartFridge.fridge.domain.Type;
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
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResDto {
    private Long id;
    private String content;
    private boolean isRead;
    private NotificationType type;
    private Type fridgeMemberType;
    private TargetType targetType;
    private Long targetId;
    private LocalDateTime createdAt;

    public static NotificationResDto fromEntity(Notification notification, Type fridgeMemberType) {
        return NotificationResDto.builder()
                .id(notification.getId())
                .content(notification.getContent())
                .isRead(notification.isRead())
                .type(notification.getNotificationType())
                .fridgeMemberType(fridgeMemberType)
                .targetType(notification.getTargetType())
                .targetId(notification.getTargetId())
                .createdAt(notification.getCreatedTime())
                .build();
    }
}
