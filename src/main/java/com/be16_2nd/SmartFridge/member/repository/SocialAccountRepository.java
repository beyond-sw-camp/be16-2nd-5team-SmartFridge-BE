package com.be16_2nd.SmartFridge.member.repository;

import com.be16_2nd.SmartFridge.member.domain.SocialAccount;
import com.be16_2nd.SmartFridge.member.domain.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findBySocialTypeAndSocialId(SocialType socialType, String socialId);
}
