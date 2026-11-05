package com.be16_2nd.SmartFridge.inquiry.exception;

// 이미 답변된 문의에 다시 답변하거나, 허용되지 않는 상태로 바꾸려 할 때 발생 (409)
public class InquiryConflictException extends RuntimeException {

    public InquiryConflictException(String message) {
        super(message);
    }
}
