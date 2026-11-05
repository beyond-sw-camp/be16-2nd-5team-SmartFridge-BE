package com.be16_2nd.SmartFridge.chat.repository;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

// 채팅방별 개수 (참여자 수, 안 읽은 메시지 수 등을 GROUP BY 로 한 번에 셀 때 사용)
public interface RoomCount {
    Long getRoomId();
    Long getCount();

    // 결과에 없는 방은 0 으로 본다
    static Map<Long, Long> toMap(Collection<RoomCount> counts) {
        return counts.stream().collect(Collectors.toMap(RoomCount::getRoomId, RoomCount::getCount));
    }
}
