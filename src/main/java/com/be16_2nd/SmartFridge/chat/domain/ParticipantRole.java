package com.be16_2nd.SmartFridge.chat.domain;

/*
 * 채팅 참여자 역할
 * - MEMBER : 일반 참여자 (1대1 채팅방에서는 방의 주인인 냉장고 멤버)
 * - MANAGER : 1대1 채팅방의 상대인 냉장고 관리자 (관리자 위임 시 교체됨)
 */
public enum ParticipantRole {
    MEMBER, MANAGER
}
