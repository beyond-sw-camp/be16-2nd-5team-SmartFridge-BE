package com.be16_2nd.SmartFridge.notification.repository;

import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.notification.domain.Notification;
import com.be16_2nd.SmartFridge.notification.domain.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAll(Specification<Notification> specification, Pageable pageable);

    // Admin 알림 조회를 위한 새로운 메서드들

    // Admin의 안읽은 알림 조회 (최신순 정렬)
    List<Notification> findByReceiverAndIsReadFalseOrderByCreatedTimeDesc(Member receiver);

    // Admin의 읽은 알림 조회 (최신순 정렬)
    List<Notification> findByReceiverAndIsReadTrueOrderByCreatedTimeDesc(Member receiver);

    // Admin의 모든 알림 조회 (읽음 상태별 정렬)
    List<Notification> findByReceiverOrderByIsReadAscCreatedTimeDesc(Member receiver);

    // Admin의 안읽은 알림 개수 조회
    long countByReceiverAndIsReadFalse(Member receiver);

    List<Notification> findTop5ByReceiverAndFridgeIdAndIsReadFalseAndNotificationTypeInOrderByCreatedTimeDesc(
            Member receiver,
            Long fridgeId,
            List<NotificationType> notificationTypes
    );
}
