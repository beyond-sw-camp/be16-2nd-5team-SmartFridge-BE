package com.be16_2nd.SmartFridge.chat.repository;

import com.be16_2nd.SmartFridge.chat.domain.JoinStatus;
import com.be16_2nd.SmartFridge.chat.domain.PurchasePost;
import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchasePostRepository extends JpaRepository<PurchasePost, Long> {
    Optional<PurchasePost> findByChatRoomId(Long chatRoomId);
    // 모집 중인 공동구매 목록: 방·방장을 함께 조회
    @Query("select pp from PurchasePost pp join fetch pp.chatRoom r join fetch pp.creator " +
            "where r.fridge = :fridge and pp.joinStatus = :joinStatus and pp.isActive = true " +
            "order by pp.createdTime desc")
    List<PurchasePost> findOpenPostsWithRoomAndCreator(@Param("fridge") Fridge fridge, @Param("joinStatus") JoinStatus joinStatus);
}
