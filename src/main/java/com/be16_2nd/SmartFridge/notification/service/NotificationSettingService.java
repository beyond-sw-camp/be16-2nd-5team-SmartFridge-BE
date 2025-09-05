package com.be16_2nd.SmartFridge.notification.service;

import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.member.repository.MemberRepository;
import com.be16_2nd.SmartFridge.notification.domain.NotificationSetting;
import com.be16_2nd.SmartFridge.notification.domain.NotificationSettingType;
import com.be16_2nd.SmartFridge.notification.dto.NotificationSettingDto;
import com.be16_2nd.SmartFridge.notification.dto.NotificationSettingReqDto;
import com.be16_2nd.SmartFridge.notification.repository.NotificationSettingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final MemberRepository memberRepository;
    
    // 알림 수신 여부 설정
    public void setNotification(NotificationSettingReqDto notificationSettingReqDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("가입되지 않은 사용자입니다."));

        // 기존 설정 조회
        Map<NotificationSettingType, NotificationSetting> notificationSettingMap =
                notificationSettingRepository.findAllByMember(member).stream()
                        .collect(Collectors.toMap(NotificationSetting::getNotificationSettingType
                                , setting -> setting));

        
        for (Map.Entry<String, Boolean> entry : notificationSettingReqDto.getSettings().entrySet()) {
            String notificationType = entry.getKey();
            boolean isActive = entry.getValue();

            try {
                // String notificationType -> ENUM 타입으로 변경
                NotificationSettingType notificationSettingType = NotificationSettingType
                        .valueOf(notificationType.toUpperCase());

                // 사용자의 기존 알림 수신 설정 확인
                NotificationSetting notificationSetting = notificationSettingMap.get(notificationSettingType);

                if (notificationSetting != null) {
                    // 기존 설정이 있으면 isActive 만 변경
                    notificationSetting.setActive(isActive);
                } else {
                    // 기존 설정 없으면 알림 수신 설정 객체 생성 후 저장
                    notificationSetting = NotificationSetting.builder()
                            .member(member)
                            .notificationSettingType(notificationSettingType)
                            .isActive(isActive)
                            .build();
                    notificationSettingRepository.save(notificationSetting);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("알림 수신 설정 실패");
            }

        }
    }


    public boolean isNotificationActive(Member member, NotificationSettingType type) {
        // db에 사용자가 설정한 알림 설정이 있으면 해당 값을, 없으면 기본값인 true 반환
        boolean isActive = notificationSettingRepository.findByMemberAndNotificationSettingType(member, type)
                .map(NotificationSetting::isActive)
                .orElse(true);

        return isActive;
    }

}
