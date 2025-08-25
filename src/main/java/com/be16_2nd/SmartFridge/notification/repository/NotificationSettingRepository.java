package com.be16_2nd.SmartFridge.notification.repository;

import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.notification.domain.NotificationSetting;
import com.be16_2nd.SmartFridge.notification.domain.NotificationSettingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    List<NotificationSetting> findAllByMember(Member member);

    Optional<NotificationSetting> findByMemberAndNotificationSettingType(Member member, NotificationSettingType notificationSettingType);
}
