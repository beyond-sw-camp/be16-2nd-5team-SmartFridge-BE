package com.be16_2nd.SmartFridge.chat.dto;

import com.be16_2nd.SmartFridge.chat.domain.ChatRoom;
import com.be16_2nd.SmartFridge.chat.domain.PurchasePost;
import com.be16_2nd.SmartFridge.member.domain.Member;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ChatRoomCreateDto {
    static final int MAX_PARTICIPANTS = 20; // 프론트 CreateRoomModal 과 같은 값

    @NotNull(message = "냉장고를 선택해 주세요")
    private Long fridgeId;

    @NotBlank(message = "제목을 입력해 주세요")
    @Size(max = 100, message = "제목은 100자 이하로 입력해 주세요")
    private String title;

    @Size(max = 2000, message = "내용은 2000자 이하로 입력해 주세요")
    private String contents;

    // 개설자 포함 인원
    @NotNull(message = "모집 인원을 입력해 주세요")
    @Min(value = 2, message = "모집 인원은 2명 이상이어야 합니다")
    @Max(value = MAX_PARTICIPANTS, message = "모집 인원은 " + MAX_PARTICIPANTS + "명 이하여야 합니다")
    private Integer maxParticipants;

    @Size(max = 255, message = "상품 링크가 너무 깁니다")
    @Pattern(regexp = "^(https?://.*)?$", message = "상품 링크는 http(s) 주소여야 합니다")
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
