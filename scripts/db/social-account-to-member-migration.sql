-- 소셜 계정 정보를 social_account 테이블에서 member 테이블로 되돌린다
--
-- 같은 이메일이면 계정을 합치지 않기로 하면서 회원 1명 = 소셜 계정 1개가 되어
-- social_account(1:N) 테이블이 필요 없어졌다. member.social_type / member.social_id 를 다시 쓴다.
--
-- 운영(prod)은 ddl-auto: update 라서 member.social_type / social_id 컬럼(없으면)과
-- 유니크 제약은 앱 기동 시 생성되지만, social_account 의 데이터는 옮겨지지 않는다.
-- 옮기지 않으면 social_account 로만 연결된 회원이 "이미 가입된 이메일입니다" 오류로 로그인하지 못하므로,
-- 새 버전 배포 직후 아래를 실행한다.
--
-- 실행 순서
--   1) 새 버전 배포
--   2) [1] 정리 → [2] 이관 실행 후 [3] 검증 쿼리로 건수 확인
--   3) 문제가 없으면 [4] 로 social_account 테이블 삭제 (선택)

-- [1] 예전 일반(LOCAL) 가입 값 정리
--     SocialType enum 에 LOCAL 이 없어서 남아 있으면 회원 조회 시 매핑 오류가 난다
UPDATE member
SET social_type = NULL, social_id = NULL
WHERE social_type IS NOT NULL
  AND social_type NOT IN ('KAKAO', 'NAVER', 'GOOGLE');

-- [2] social_account → member 이관
--     계정 합치기 기간에 한 회원에 여러 소셜 계정이 연결됐다면 가장 먼저 연결한 계정을 쓴다
UPDATE member m
JOIN social_account sa
  ON sa.id = (SELECT MIN(s2.id) FROM social_account s2 WHERE s2.member_id = m.id)
SET m.social_type = sa.social_type,
    m.social_id   = sa.social_id;

-- [3] 검증
-- 3-1. 두 값이 같아야 한다 (social_account 가 있는 회원 수 = 소셜 정보가 채워진 회원 수 중 해당 회원)
SELECT COUNT(DISTINCT member_id) AS members_in_social_account FROM social_account;
SELECT COUNT(*) AS migrated_members
FROM member m
WHERE m.social_type IS NOT NULL
  AND EXISTS (SELECT 1 FROM social_account sa WHERE sa.member_id = m.id);

-- 3-2. 소셜 계정이 2개 이상이었던 회원: 첫 번째 계정만 남으므로 나머지 계정으로는 로그인할 수 없다
SELECT sa.member_id, m.email, COUNT(*) AS account_count
FROM social_account sa JOIN member m ON m.id = sa.member_id
GROUP BY sa.member_id, m.email
HAVING COUNT(*) > 1;

-- 3-3. 소셜 정보가 없는 일반 회원 (로그인 불가, "관리자에게 문의" 안내 대상)
SELECT id, email, name FROM member
WHERE social_type IS NULL AND role <> 'ADMIN';

-- 3-4. 유니크 제약 확인 (uk_member_social_type_id 가 있어야 한다)
SHOW INDEX FROM member WHERE Key_name = 'uk_member_social_type_id';
-- 없으면:
-- ALTER TABLE member ADD CONSTRAINT uk_member_social_type_id UNIQUE (social_type, social_id);

-- [4] 정리 (선택, 이관 확인 후)
-- DROP TABLE social_account;
