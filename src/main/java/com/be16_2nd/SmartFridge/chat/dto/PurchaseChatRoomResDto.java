package com.be16_2nd.SmartFridge.chat.dto;

import com.be16_2nd.SmartFridge.chat.domain.PurchaseChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchaseChatRoomResDto {
    private String roomName;
    private String userName;
    private String createdAt;
    private String contents;
    private Integer currentParticipants;
    private Integer maxParticipants;
    private String joinStatus;
    private String productUrl;

    public static PurchaseChatRoomResDto fromPurchaseChatRoom(PurchaseChatRoom purchaseChatRoom) {
        return PurchaseChatRoomResDto.builder()
                .roomName(purchaseChatRoom.getTitle())
                .userName(purchaseChatRoom.getCreator().getName())
                .createdAt(purchaseChatRoom.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH시 mm분")))
                .contents(purchaseChatRoom.getContents())
                .currentParticipants(purchaseChatRoom.getCurrentParticipants())
                .maxParticipants(purchaseChatRoom.getMaxParticipants())
                .joinStatus(purchaseChatRoom.getJoinStatus().toString())
                .productUrl(purchaseChatRoom.getProductUrl())
                .build();
    }
}
