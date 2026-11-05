-- 냉장고를 떠난 회원이 공동구매 채팅방에 남아 있는 데이터 정리
--
-- 예전에는 냉장고를 스스로 나가면(leaveFridge) 1대1 채팅방만 지우고 공동구매 채팅방 참여 정보는 남겼다.
-- 코드 수정 후에는 새로 생기지 않지만, 이미 남은 데이터는 아래로 정리한다.
--   - 방장이 냉장고를 떠난 방 : 방장이 나가면 방을 지우는 규칙대로 방 전체 삭제
--   - 그 밖의 방 : 냉장고를 떠난 참여자만 제거
-- 코드에서도 냉장고 멤버가 아니면 채팅방 접근을 거부하므로, 정리 전에도 접근은 막힌다.
--
-- 실행 순서 : [1] 확인 → [2] 삭제 (트랜잭션) → [3] 검증

-- [1] 확인
-- 1-1. 방장이 냉장고를 떠난 공동구매 방
SELECT cr.id AS chat_room_id, cr.fridge_id, pp.creator_id, pp.title
FROM chat_room cr
JOIN purchase_post pp ON pp.chat_room_id = cr.id
WHERE cr.type = 'PURCHASE'
  AND NOT EXISTS (SELECT 1 FROM fridge_member fm WHERE fm.fridge_id = cr.fridge_id AND fm.member_id = pp.creator_id);

-- 1-2. 냉장고를 떠난 참여자 (방장 제외)
SELECT cp.id AS chat_participant_id, cp.chat_room_id, cp.member_id
FROM chat_participant cp
JOIN chat_room cr ON cr.id = cp.chat_room_id
WHERE cr.type = 'PURCHASE'
  AND NOT EXISTS (SELECT 1 FROM fridge_member fm WHERE fm.fridge_id = cr.fridge_id AND fm.member_id = cp.member_id);

-- [2] 삭제
START TRANSACTION;

CREATE TEMPORARY TABLE tmp_orphan_purchase_room AS
SELECT cr.id
FROM chat_room cr
JOIN purchase_post pp ON pp.chat_room_id = cr.id
WHERE cr.type = 'PURCHASE'
  AND NOT EXISTS (SELECT 1 FROM fridge_member fm WHERE fm.fridge_id = cr.fridge_id AND fm.member_id = pp.creator_id);

-- 2-1. 방장이 떠난 방 삭제 (FK 순서: 이미지 → 메시지 → 참여자 → 게시글 → 방)
DELETE i FROM chat_message_image i
JOIN chat_message m ON m.id = i.chat_message_id
WHERE m.chat_room_id IN (SELECT id FROM tmp_orphan_purchase_room);
DELETE FROM chat_message     WHERE chat_room_id IN (SELECT id FROM tmp_orphan_purchase_room);
DELETE FROM chat_participant WHERE chat_room_id IN (SELECT id FROM tmp_orphan_purchase_room);
DELETE FROM purchase_post    WHERE chat_room_id IN (SELECT id FROM tmp_orphan_purchase_room);
DELETE FROM chat_room        WHERE id IN (SELECT id FROM tmp_orphan_purchase_room);

-- 2-2. 남은 방에서 냉장고를 떠난 참여자 제거
DELETE cp FROM chat_participant cp
JOIN chat_room cr ON cr.id = cp.chat_room_id
WHERE cr.type = 'PURCHASE'
  AND NOT EXISTS (SELECT 1 FROM fridge_member fm WHERE fm.fridge_id = cr.fridge_id AND fm.member_id = cp.member_id);

DROP TEMPORARY TABLE tmp_orphan_purchase_room;

COMMIT;

-- [3] 검증 : 두 값 모두 0 이어야 한다
SELECT COUNT(*) AS stale_participants
FROM chat_participant cp
JOIN chat_room cr ON cr.id = cp.chat_room_id
WHERE cr.type = 'PURCHASE'
  AND NOT EXISTS (SELECT 1 FROM fridge_member fm WHERE fm.fridge_id = cr.fridge_id AND fm.member_id = cp.member_id);
SELECT COUNT(*) AS orphan_rooms
FROM chat_room cr
JOIN purchase_post pp ON pp.chat_room_id = cr.id
WHERE cr.type = 'PURCHASE'
  AND NOT EXISTS (SELECT 1 FROM fridge_member fm WHERE fm.fridge_id = cr.fridge_id AND fm.member_id = pp.creator_id);
