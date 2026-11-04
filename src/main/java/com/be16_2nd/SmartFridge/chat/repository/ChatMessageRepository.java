package com.be16_2nd.SmartFridge.chat.repository;

import com.be16_2nd.SmartFridge.chat.domain.ChatMessage;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByChatRoom(ChatRoom chatRoom, Pageable pageable);

    // 읽음 커서 이후에 온, 내가 보내지 않은 메시지 수
    @Query("select count(m) from ChatMessage m " +
            "where m.chatRoom.id = :roomId and m.id > :lastReadMessageId " +
            "and (m.sender is null or m.sender.id <> :memberId)")
    long countUnread(@Param("roomId") Long roomId,
                     @Param("lastReadMessageId") Long lastReadMessageId,
                     @Param("memberId") UUID memberId);

    @Query("select coalesce(max(m.id), 0) from ChatMessage m where m.chatRoom.id = :roomId")
    Long findLastMessageId(@Param("roomId") Long roomId);
}
