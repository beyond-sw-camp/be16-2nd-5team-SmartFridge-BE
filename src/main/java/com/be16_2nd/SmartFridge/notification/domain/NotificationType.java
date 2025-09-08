package com.be16_2nd.SmartFridge.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    NEW_FOOD("새로운 식품이 등록되었습니다.", NotificationSettingType.SYSTEM),
    NEW_MEMBER("새로운 멤버가 참여했습니다.", NotificationSettingType.SYSTEM),
    EXPIRATION_IMMINENT("유통기한 임박 알림", NotificationSettingType.SYSTEM),
    NEW_ANNOUNCEMENT("새로운 공지사항이 등록되었습니다.", NotificationSettingType.ANNOUNCE),
    NEW_COMMENT("내 게시글에 새로운 댓글이 달렸습니다.", NotificationSettingType.COMMENT),
    NEW_INQUIRY("새로운 문의가 등록되었습니다.", NotificationSettingType.SYSTEM),
    ADMIN_REPLY("문의하신 글에 관리자 답변이 달렸습니다.", NotificationSettingType.SYSTEM),
    MANAGER_CHAT("관리자 채팅방에 새로운 메시지가 도착했습니다.", NotificationSettingType.CHAT),
    GROUP_CHAT("공동 구매 채팅방에 새로운 메시지가 도착했습니다.", NotificationSettingType.CHAT),
    ROOM_FULL(" 채팅방에 인원이 마감되었습니다.", NotificationSettingType.CHAT),
    ROOM_CLOSED(" 채팅방이 종료되었습니다.", NotificationSettingType.CHAT);

    private final String description;
    private final NotificationSettingType settingType;

    // 알림 목록에 표시할 타입
    public static List<NotificationType> visibleInNotificationList() {
        return List.of(
                NEW_FOOD,
                NEW_MEMBER,
                EXPIRATION_IMMINENT,
                NEW_ANNOUNCEMENT,
                NEW_COMMENT,
                ROOM_FULL,
                ROOM_CLOSED,
                ADMIN_REPLY
        );
    }
}
