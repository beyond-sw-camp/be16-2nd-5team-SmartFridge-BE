package com.be16_2nd.SmartFridge.inquiry.domain;

import java.util.EnumSet;
import java.util.Set;

public enum InquiryStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED;

    // 상태는 앞으로만 진행된다. 완료/취소된 문의는 다른 상태로 바꿀 수 없다.
    public boolean canTransitTo(InquiryStatus next) {
        return next != null && allowedNext().contains(next);
    }

    private Set<InquiryStatus> allowedNext() {
        return switch (this) {
            case PENDING -> EnumSet.of(IN_PROGRESS, COMPLETED, CANCELLED);
            case IN_PROGRESS -> EnumSet.of(COMPLETED, CANCELLED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(InquiryStatus.class);
        };
    }
}
