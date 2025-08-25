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
    ADMIN_CHAT("관리자로부터 새로운 메시지가 도착했습니다.", NotificationSettingType.CHAT),
    GROUP_CHAT("그룹 채팅에 새로운 메시지가 도착했습니다.", NotificationSettingType.CHAT);

    private final String description;
    private final NotificationSettingType settingType;

    // 알림 목록에 표시할 타입 (문의/채팅 제외)
    public static List<NotificationType> visibleInNotificationList() {
        return List.of(
                NEW_FOOD,
                NEW_MEMBER,
                EXPIRATION_IMMINENT,
                NEW_ANNOUNCEMENT,
                NEW_COMMENT
        );
    }

    // 문의 관련 타입
    public static List<NotificationType> inquiryTypes() {
        return List.of(NEW_INQUIRY, ADMIN_REPLY);
    }

    // 채팅 관련 타입
    public static List<NotificationType> chatTypes() {
        return List.of(ADMIN_CHAT, GROUP_CHAT);
    }
}
