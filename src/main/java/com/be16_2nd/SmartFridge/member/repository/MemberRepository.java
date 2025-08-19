package com.be16_2nd.SmartFridge.member.repository;

import com.be16_2nd.SmartFridge.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findBySocialId(String socialId);
}
