-- 소셜 로그인 전용 전환: member.social_type / member.social_id -> social_account 테이블
--
-- 운영(prod)은 ddl-auto: update 라서 social_account 테이블은 앱 기동 시 자동 생성되지만,
-- 기존 회원의 소셜 연결 정보는 옮겨지지 않는다. 옮기지 않으면 기존 소셜 회원이
-- "이미 가입된 이메일입니다" 오류로 로그인하지 못하므로, 새 버전 배포 직후 아래를 실행한다.
--
-- 실행 순서
--   1) 새 버전 배포 (social_account 테이블 생성 확인)
--   2) 아래 [1] 이관 실행 후 [2] 검증 쿼리로 건수 확인
--   3) 문제가 없으면 [3] 으로 기존 컬럼 정리 (선택)

-- [1] 기존 소셜 회원 연결 정보 이관
INSERT INTO social_account (member_id, social_type, social_id, created_time, updated_time)
SELECT m.id, m.social_type, m.social_id, NOW(), NOW()
FROM member m
WHERE m.social_type IN ('KAKAO', 'NAVER', 'GOOGLE')
  AND m.social_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM social_account sa
      WHERE sa.social_type = m.social_type AND sa.social_id = m.social_id
  );

-- [2] 검증: 두 값이 같아야 한다
SELECT COUNT(*) AS legacy_social_members
FROM member WHERE social_type IN ('KAKAO', 'NAVER', 'GOOGLE') AND social_id IS NOT NULL;
SELECT COUNT(*) AS migrated_social_accounts FROM social_account;

-- 참고: 일반(LOCAL) 가입 회원은 더 이상 로그인할 수 없다. 대상자 확인용
SELECT id, email, name FROM member
WHERE (social_type = 'LOCAL' OR social_type IS NULL) AND role <> 'ADMIN';

-- [3] 정리 (선택, 이관 확인 후)
-- ALTER TABLE member DROP COLUMN social_type;
-- ALTER TABLE member DROP COLUMN social_id;
