-- 문의 1건당 답변 1개 제약(inquiry_comment.inquiry_id UNIQUE) 적용 전 정리 스크립트
-- 배포 전에 운영 DB에서 1 → 2 → 3 순서로 실행한다.

-- 1. 중복 답변 확인 (결과가 없으면 2번은 건너뛴다)
SELECT inquiry_id, COUNT(*) AS reply_count, MIN(comment_id) AS keep_comment_id
FROM inquiry_comment
GROUP BY inquiry_id
HAVING COUNT(*) > 1;

-- 2. 문의별로 가장 먼저 작성된 답변(comment_id 가 가장 작은 것)만 남기고 삭제
DELETE c
FROM inquiry_comment c
JOIN (
    SELECT inquiry_id, MIN(comment_id) AS keep_comment_id
    FROM inquiry_comment
    GROUP BY inquiry_id
    HAVING COUNT(*) > 1
) d ON d.inquiry_id = c.inquiry_id
WHERE c.comment_id <> d.keep_comment_id;

-- 3. UNIQUE 제약 추가 (엔티티와 같은 이름. ddl-auto 가 먼저 추가했다면 이미 존재한다는 오류가 나며, 무시해도 된다)
ALTER TABLE inquiry_comment ADD CONSTRAINT uk_inquiry_comment_inquiry_id UNIQUE (inquiry_id);
