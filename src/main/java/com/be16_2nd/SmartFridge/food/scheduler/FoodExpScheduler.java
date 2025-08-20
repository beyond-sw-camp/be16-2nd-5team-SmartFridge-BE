package com.be16_2nd.SmartFridge.food.scheduler;

import com.be16_2nd.SmartFridge.food.domain.Food;
import com.be16_2nd.SmartFridge.food.repository.FoodRepository;
import com.be16_2nd.SmartFridge.fridge.domain.FridgeMember;
import com.be16_2nd.SmartFridge.member.domain.Member;
import com.be16_2nd.SmartFridge.notification.domain.Notification;
import com.be16_2nd.SmartFridge.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FoodExpScheduler  {

    private final FoodRepository foodRepository;
    private final NotificationService notificationService;

    // 매일 오전 9시에 실행
    @Transactional
    @Scheduled(cron = "0 0 10 * * *")
    public void notificationForExpFood() {

        // 유통기한 당일
        LocalDate today = LocalDate.now();
        // 유통기한 하루 전 (현재 + 1일)
        LocalDate oneDayAfter = today.plusDays(1);
        // 유통기한 삼일 전 (현재 + 3일)
        LocalDate threeDaysAfter = today.plusDays(3);

        // 유통기한이 현재 날짜를 기준으로 당일, 하루, 삼일 후인 음식 조회
        List<Food> expFoodList = foodRepository.findAllByExpirationDateIn(
                List.of(today, oneDayAfter, threeDaysAfter)
        );

        for (Food food : expFoodList) {
            
            // 식자재 등록한 회원
            Member receiver = food.getMember();

            // 유통기한 임박 기간에 따른 메세지 조합
            String daysLeftMessage = getDaysLeftMessage(food.getExpirationDateTime().toLocalDate(), today);
            // 알림 객체 생성 후 메세지 발송
            Notification notification = Notification.fromExpiration(receiver, food, daysLeftMessage);
            // 알림 db에 저장
            notificationService.create(notification);
        }

    }

    private String getDaysLeftMessage(LocalDate expiryDate, LocalDate today) {
        if (expiryDate.isEqual(today)) {
            return "오늘까지입니다.";
        }
        if (expiryDate.isEqual(today.plusDays(1))) {
            return "1일 남았습니다.";
        }
        if (expiryDate.isEqual(today.plusDays(3))) {
            return "3일 남았습니다.";
        }
        return "";
    }
}
