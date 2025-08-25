package com.be16_2nd.SmartFridge.fridge.repository;

import com.be16_2nd.SmartFridge.fridge.domain.Fridge;
import com.be16_2nd.SmartFridge.fridge.domain.FridgeMember;
import com.be16_2nd.SmartFridge.fridge.domain.Type;
import com.be16_2nd.SmartFridge.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FridgeMemberRepository extends JpaRepository<FridgeMember, Long> {
    boolean existsByFridgeAndMember(Fridge fridge, Member member);
    void deleteByFridgeAndMember(Fridge fridge, Member member);
    List<FridgeMember> findAllByMember(Member member);
    Optional<FridgeMember> findByFridgeAndMember(Fridge fridge, Member member);   //Optional 로바꿈 -찬진
    List<FridgeMember> findByFridge(Fridge fridge);
    Optional<FridgeMember> findByFridgeAndType(Fridge fridge, Type type);
    List<FridgeMember> findAllByFridgeAndType(Fridge fridge, Type type);
}
