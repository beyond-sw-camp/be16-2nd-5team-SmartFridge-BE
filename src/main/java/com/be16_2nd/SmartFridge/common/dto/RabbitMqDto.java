package com.be16_2nd.SmartFridge.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RabbitMqDto {
    private Long postId;
    private UUID memberId;
    private StatsType statsType;

    public enum StatsType {
        VIEW, LIKE, UNLIKE
    }
}
