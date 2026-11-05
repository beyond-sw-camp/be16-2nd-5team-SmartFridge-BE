package com.be16_2nd.SmartFridge.chat.repository;

import com.be16_2nd.SmartFridge.chat.domain.ChatRoom;
import com.be16_2nd.SmartFridge.chat.domain.ChatRoomType;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByIdAndType(Long id, ChatRoomType type);

    // 공동구매 참여 시 정원 확인을 직렬화하기 위한 비관적 잠금 (SELECT ... FOR UPDATE)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ChatRoom r where r.id = :id and r.type = :type")
    Optional<ChatRoom> findByIdAndTypeForUpdate(@Param("id") Long id, @Param("type") ChatRoomType type);
    List<ChatRoom> findByFridgeAndType(Fridge fridge, ChatRoomType type);
}
