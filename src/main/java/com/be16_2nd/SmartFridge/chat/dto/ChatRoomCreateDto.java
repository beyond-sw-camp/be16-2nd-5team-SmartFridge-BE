package com.be16_2nd.SmartFridge.chat.dto;

import com.be16_2nd.SmartFridge.chat.domain.ChatRoom;
import com.be16_2nd.SmartFridge.chat.domain.PurchasePost;
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

    public PurchasePost toPurchasePost(Member member, ChatRoom chatRoom) {
        return PurchasePost.builder()
                .creator(member)
                .chatRoom(chatRoom)
                .contents(this.getContents())
                .title(this.getTitle())
                .maxParticipants(this.getMaxParticipants())
                .productUrl(this.getProductUrl())
                .build();
    }
}
