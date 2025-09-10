package com.be16_2nd.SmartFridge.fridge.repository;

import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FridgeRepository extends JpaRepository<Fridge, Long> {
    Optional<Fridge> findByFridgeName(String fridgeName);

    Optional<Fridge> findByInviteCode(String inviteCode);

    boolean existsByFridgeNameAndFridgeMemberList_Member(String fridgeName, Member member);
}
