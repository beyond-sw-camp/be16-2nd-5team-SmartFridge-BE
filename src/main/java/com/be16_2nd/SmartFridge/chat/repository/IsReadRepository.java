package com.be16_2nd.SmartFridge.chat.repository;

import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.chat.domain.IsRead;
import com.be16_2nd.SmartFridge.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IsReadRepository extends JpaRepository<IsRead, Long> {
    List<IsRead> findAllByRoomIdAndMemberAndChatRoomType(Long roomId, Member member, ChatRoomType chatRoomType);
    Long countByMemberAndRoomIdAndChatRoomTypeAndIsReadFalse(Member member, Long roomId, ChatRoomType chatRoomType);
    List<IsRead> findAllByRoomIdAndMemberAndChatRoomTypeAndIsReadFalse(Long roomId, Member member, ChatRoomType chatRoomType);
}
