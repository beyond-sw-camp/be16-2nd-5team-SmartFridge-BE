package com.be16_2nd.SmartFridge.chat.dto;

import com.be16_2nd.SmartFridge.chat.domain.PurchaseChatRoom;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ChatRoomCreateDto {
    private Long fridgeId;
    private String title;
    private String contents;
    private Integer maxParticipants;
    private String productUrl;

    public PurchaseChatRoom toPurchaseChatRoom(Member member, Fridge fridge) {
        return PurchaseChatRoom.builder()
                .creator(member)
                .fridge(fridge)
                .contents(this.getContents())
                .title(this.getTitle())
                .currentParticipants(1)
                .maxParticipants(this.getMaxParticipants())
                .productUrl(this.getProductUrl())
                .build();
    }
}
