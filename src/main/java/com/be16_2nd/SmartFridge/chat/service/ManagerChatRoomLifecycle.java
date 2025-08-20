package com.be16_2nd.SmartFridge.chat.service;

import com.be16_2nd.SmartFridge.chat.domain.ManagerChatRoom;
import com.be16_2nd.SmartFridge.chat.dto.ChatRoomCreateDto;
import com.be16_2nd.SmartFridge.chat.repository.ManagerChatRoomRepository;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Transactional
@RequiredArgsConstructor
public class ManagerChatRoomLifecycle {

    // 현재 채팅방 참여자 담기 <채팅방ID, 회원이메일>
    public static Map<Long, Set<String>> ManagerRoomParticipants = new ConcurrentHashMap<>();
    public static Map<Long, Set<String>> PurchaseRoomParticipants = new ConcurrentHashMap<>();


    private final ManagerChatRoomRepository managerChatRoomRepository;

    // 1대1 채팅방 생성
    public void createManagerChatRoom(Member member, Fridge fridge){
        ManagerChatRoom newChatRoom = ManagerChatRoom.builder()
                .fridge(fridge)
                .member(member)
                .build();
        managerChatRoomRepository.save(newChatRoom);
    }

    // 냉장고 나갈 시 채팅방 삭제
    public void deleteManagerChatRoom(Member member, Fridge fridge){
        // 1대1채팅
        managerChatRoomRepository.deleteByFridgeAndMember(fridge, member);
    }
    
    // 회원 탈퇴 시 모든 채팅방 삭제
}
