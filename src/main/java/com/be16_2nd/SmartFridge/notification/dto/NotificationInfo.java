package com.be16_2nd.SmartFridge.notification.dto;

import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.notification.domain.TargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationInfo {

    private String contents;
    private TargetType targetType;
    private Long targetId;
    private Fridge fridge;

}
