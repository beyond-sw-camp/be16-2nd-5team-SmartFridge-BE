package com.be16_2nd.SmartFridge.member.dto;

import com.be16_2nd.SmartFridge.notification.dto.NotificationResDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminNotificationListDto {
    private List<NotificationResDto> unreadNotifications;
    private List<NotificationResDto> readNotifications;
    private int unreadCount;
    private int totalCount;
}
