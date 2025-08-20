package com.be16_2nd.SmartFridge.notification.service;

import com.be16_2nd.SmartFridge.notification.domain.Notification;
import com.be16_2nd.SmartFridge.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;

    public void create(Notification notification) {
        
        // 유통기한 알림인 경우 발신자가 시스템이기 때문에 sender가 null -> 분기 처리 필요
        String senderEmail;
        if (notification.getSender() == null) {
            senderEmail = null;
        } else {
            senderEmail = notification.getSender().getEmail();
        }
        
        // 알림 발송
        notificationPublisher.publish(
                senderEmail
                , notification.getReceiver().getEmail()
                , notification.getContent()
                , notification.getNotificationType().name()
        );

        // 알림 db에 저장
        notificationRepository.save(notification);
    }

}
