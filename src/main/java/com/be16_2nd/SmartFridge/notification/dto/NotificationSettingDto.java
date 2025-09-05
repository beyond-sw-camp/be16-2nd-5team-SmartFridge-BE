package com.be16_2nd.SmartFridge.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSettingDto {
    private String notificationSettingType;
    private boolean isActive;
}
