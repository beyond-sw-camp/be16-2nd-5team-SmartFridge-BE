package com.be16_2nd.SmartFridge.notification.service;

import com.be16_2nd.SmartFridge.Post.domain.Post;
import com.be16_2nd.SmartFridge.Post.domain.PostComment;
import com.be16_2nd.SmartFridge.common.service.FridgeAccessValidator;
import com.be16_2nd.SmartFridge.food.domain.Food;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.domain.FridgeMember;
import com.be16_2nd.SmartFridge.fridge.domain.Type;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import com.be16_2nd.SmartFridge.inquiryComment.domain.InquiryComment;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.notification.domain.Notification;
import com.be16_2nd.SmartFridge.notification.domain.NotificationSettingType;
import com.be16_2nd.SmartFridge.notification.domain.NotificationType;
import com.be16_2nd.SmartFridge.notification.domain.TargetType;
import com.be16_2nd.SmartFridge.notification.dto.NotificationReadReqDto;
import com.be16_2nd.SmartFridge.notification.dto.NotificationResDto;
import com.be16_2nd.SmartFridge.notification.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;
    private final NotificationSettingService notificationSettingService;
    private final FridgeAccessValidator fridgeAccessValidator;
    private final FridgeMemberRepository fridgeMemberRepository;

    public void create(Member sender, Member receiver,
                              NotificationType notificationType, Object entity) {

        String content;
        TargetType targetType;
        Long targetId;
        Fridge fridge = null;


        // 알림 종류에 따른 알림 객체 조립
        switch (notificationType) {
            case NEW_MEMBER:
                fridge = (Fridge) entity;
                content = fridge.getFridgeName() + "에 새로운 멤버 " +
                        sender.getName() + "님이 참여하였습니다.";
                targetType = TargetType.FRIDGE;
                targetId = fridge.getId();
                break;

            case NEW_FOOD:
                Food food = (Food) entity;
                content = sender.getName() + "님이 새로운 식품 '" + food.getName() + "'을(를) 등록했습니다.";
                targetType = TargetType.FOOD;
                targetId = food.getId();
                fridge = food.getFridge();
                break;

            case NEW_ANNOUNCEMENT:
                Post post = (Post) entity;
                content = post.getFridge().getFridgeName() + "에 " +
                        notificationType.getDescription();
                targetType = TargetType.POST;
                targetId = post.getId();
                fridge = post.getFridge();
                break;

            case NEW_COMMENT:
                PostComment postComment = (PostComment) entity;
                content = sender.getName() + "님이 회원님의 게시글에 새로운 댓글을 남겼습니다.";
                targetType = TargetType.POST;
                targetId = postComment.getPost().getId();
                fridge = postComment.getPost().getFridge();
                break;

            case NEW_INQUIRY:
                Inquiry inquiry = (Inquiry) entity;
                content = sender.getName() + "님이 새로운 문의를 등록했습니다.";
                targetType = TargetType.INQUIRY;
                targetId = inquiry.getInquiryId();
                break;

            case ADMIN_REPLY:
                InquiryComment inquiryComment = (InquiryComment) entity;
                content = notificationType.getDescription();
                targetType = TargetType.INQUIRY;
                targetId = inquiryComment.getInquiry().getInquiryId();
                break;

            default:
                throw new IllegalArgumentException("지원하지 않는 알림 타입입니다. : " + notificationType);
        }

        // 알림 객체 build
        Notification notification = Notification.builder()
                .sender(sender)
                .receiver(receiver)
                .notificationType(notificationType)
                .content(content)
                .targetType(targetType)
                .targetId(targetId)
                .fridge(fridge)
                .build();

        createAndSend(notification);
    }

    // 유통기한 알림
    public void createAndSendForExpiration(Member receiver, Food food, String daysLeftMessage) {

        String content = "'" + food.getFridge().getFridgeName() + "'의 '" + food.getName() +
                "' 유통기한이 " + daysLeftMessage;

        Notification notification = Notification.builder()
                .sender(null)
                .receiver(receiver)
                .content(content)
                .notificationType(NotificationType.EXPIRATION_IMMINENT)
                .targetType(TargetType.FOOD)
                .targetId(food.getId())
                .fridge(food.getFridge())
                .build();

        createAndSend(notification);
    }

    // db 저장 알림 발송
    private void createAndSend(Notification notification) {
        // db 저장
        notificationRepository.save(notification);


        Member sender = notification.getSender();
        Member receiver = notification.getReceiver();
        NotificationSettingType notificationSettingType = notification.getNotificationType().getSettingType();
        boolean isActive = notificationSettingService.isNotificationActive(receiver, notificationSettingType);

        // 알림 수신 여부 설정에 따른 발송
        if (isActive) {
            String senderEmail = (sender != null) ? sender.getEmail() : null;

            Long fridgeId = (notification.getFridge() != null) ? notification.getFridge().getId() : null;

            notificationPublisher.publish(fridgeId, senderEmail , receiver.getEmail()
                    , notification.getContent(), notification.getNotificationType().name());
        }
    }

    // 알림 목록 조회
    public Page<NotificationResDto> findNotificationList(Long fridgeId, NotificationType notificationType, Pageable pageable) {
        FridgeAccessValidator.FridgeContext context = fridgeAccessValidator.validate(fridgeId);
        Member member = context.member();
        Fridge fridge = context.fridge();

        Type fridgeMemberType = fridgeMemberRepository.findByFridgeAndMember(fridge, member)
                .map(FridgeMember::getType)
                .orElse(null);

        Specification<Notification> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("fridge"), fridge));
            predicates.add(criteriaBuilder.equal(root.get("receiver"), member));
            predicates.add(root.get("notificationType")
                    .in(NotificationType.visibleInNotificationList())); // 문의/채팅 제외
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return notificationRepository.findAll(specification, pageable)
                .map(notification -> NotificationResDto.fromEntity(notification, fridgeMemberType));
    }

    // 문의 알림 count
    public long countUnreadInquiries(Member member) {
        return notificationRepository.countByReceiverAndNotificationTypeInAndIsReadFalse(
                member, NotificationType.inquiryTypes()
        );
    }

    // 채팅 알림 count
    public long countUnreadChats(Member member) {
        return notificationRepository.countByReceiverAndNotificationTypeInAndIsReadFalse(
                member, NotificationType.chatTypes()
        );
    }
    
    // 알림 삭제
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 알림입니다."));
        notificationRepository.delete(notification);
    }

    // 알림 읽음
    public void readNotification(List<NotificationReadReqDto> notificationReadReqDtoList) {

        if (!notificationReadReqDtoList.isEmpty()) {

            for (NotificationReadReqDto dto : notificationReadReqDtoList) {
                Notification notification = notificationRepository.findById(dto.notificationId)
                                        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 알림입니다."));

                // 읽음 처리 (isRead : false -> true)
                notification.setRead(true);
            }
        }
    }
}
