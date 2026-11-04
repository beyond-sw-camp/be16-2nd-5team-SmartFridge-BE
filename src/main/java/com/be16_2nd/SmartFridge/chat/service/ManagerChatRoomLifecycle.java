package com.be16_2nd.SmartFridge.chat.service;

import com.be16_2nd.SmartFridge.chat.domain.ChatParticipant;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoom;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.domain.ParticipantRole;
import com.be16_2nd.SmartFridge.chat.repository.ChatMessageRepository;
import com.be16_2nd.SmartFridge.chat.repository.ChatParticipantRepository;
import com.be16_2nd.SmartFridge.chat.repository.ChatRoomRepository;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.domain.FridgeMember;
import com.be16_2nd.SmartFridge.fridge.domain.Type;
import com.be16_2nd.SmartFridge.fridge.repository.FridgeMemberRepository;
import com.be16_2nd.SmartFridge.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * 1대1 관리자 채팅방 생명주기
 * - 방 주인(냉장고 멤버)은 MEMBER, 냉장고 관리자는 MANAGER 역할로 참여
 */
@Component
@Transactional
@RequiredArgsConstructor
public class ManagerChatRoomLifecycle {

    // 현재 채팅방을 보고 있는 사용자 <채팅방ID, 회원이메일>
    public static Map<Long, Set<String>> ManagerRoomParticipants = new ConcurrentHashMap<>();
    public static Map<Long, Set<String>> PurchaseRoomParticipants = new ConcurrentHashMap<>();

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final FridgeMemberRepository fridgeMemberRepository;

    // 1대1 채팅방 생성
    public void createManagerChatRoom(Member member, Fridge fridge) {
        ChatRoom chatRoom = ChatRoom.builder()
                .type(ChatRoomType.MANAGER)
                .fridge(fridge)
                .build();
        chatRoom.addParticipant(member, ParticipantRole.MEMBER);

        // 관리자 본인의 방이 아니면 관리자를 상대로 추가
        fridgeMemberRepository.findByFridgeAndType(fridge, Type.MANAGER)
                .map(FridgeMember::getMember)
                .filter(manager -> !manager.getId().equals(member.getId()))
                .ifPresent(manager -> chatRoom.addParticipant(manager, ParticipantRole.MANAGER));

        chatRoomRepository.save(chatRoom);
    }

    // 회원의 1대1 채팅방 조회
    @Transactional(readOnly = true)
    public Optional<ChatRoom> findManagerChatRoom(Fridge fridge, Member member) {
        return chatParticipantRepository
                .findByMemberAndRoleAndChatRoomFridgeAndChatRoomType(member, ParticipantRole.MEMBER, fridge, ChatRoomType.MANAGER)
                .map(ChatParticipant::getChatRoom);
    }

    // 냉장고 나갈 시 채팅방 삭제
    public void deleteManagerChatRoom(Member member, Fridge fridge) {
        findManagerChatRoom(fridge, member).ifPresent(chatRoom -> {
            fridge.getChatRooms().remove(chatRoom);
            chatRoomRepository.delete(chatRoom);
        });
    }

    // 관리자 위임 시 모든 1대1 채팅방의 관리자 참여자를 교체 (읽음 커서는 이어받음)
    public void delegateManager(Fridge fridge, Member newManager) {
        for (ChatRoom chatRoom : chatRoomRepository.findByFridgeAndType(fridge, ChatRoomType.MANAGER)) {
            Long inheritedCursor = null;
            Optional<ChatParticipant> oldManager = chatRoom.getParticipants().stream()
                    .filter(p -> p.getRole() == ParticipantRole.MANAGER)
                    .findFirst();
            if (oldManager.isPresent()) {
                inheritedCursor = oldManager.get().getLastReadMessageId();
                chatRoom.getParticipants().remove(oldManager.get());
            }

            boolean isNewManagersOwnRoom = chatRoom.getParticipants().stream()
                    .anyMatch(p -> p.getMember().getId().equals(newManager.getId()));
            if (isNewManagersOwnRoom) {
                continue;
            }

            ChatParticipant participant = chatRoom.addParticipant(newManager, ParticipantRole.MANAGER);
            participant.readUpTo(inheritedCursor != null ? inheritedCursor : chatMessageRepository.findLastMessageId(chatRoom.getId()));
        }
    }

    // 안 읽은 메시지 수 (참여자가 아니면 0)
    @Transactional(readOnly = true)
    public long countUnread(ChatRoom chatRoom, Member member) {
        return chatParticipantRepository.findByChatRoomAndMember(chatRoom, member)
                .map(p -> chatMessageRepository.countUnread(chatRoom.getId(), p.getLastReadMessageId(), member.getId()))
                .orElse(0L);
    }
}
