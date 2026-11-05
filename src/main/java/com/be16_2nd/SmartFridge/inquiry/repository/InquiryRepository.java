package com.be16_2nd.SmartFridge.inquiry.repository;

import com.be16_2nd.SmartFridge.inquiry.domain.Inquiry;
import com.be16_2nd.SmartFridge.inquiry.domain.InquiryStatus;
import com.be16_2nd.SmartFridge.member.domain.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByMember(Member member);
    List<Inquiry> findAllByMember(Member member);

    Long countAllByStatus(InquiryStatus status);

    Optional<Inquiry> findAllByInquiryId(Long inquiryId);

    // 같은 문의에 대한 답변 요청을 순서대로 처리하기 위해 문의 행을 잠근다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inquiry i where i.inquiryId = :inquiryId")
    Optional<Inquiry> findByIdForUpdate(@Param("inquiryId") Long inquiryId);
}
