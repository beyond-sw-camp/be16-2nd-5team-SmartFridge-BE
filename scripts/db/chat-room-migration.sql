-- 채팅 테이블 구조 개선 이관 스크립트 (MariaDB)
--   manager_chat_room + purchase_chat_room -> chat_room (+ purchase_post)
--   is_read                                -> chat_participant.last_read_message_id (읽음 커서)
--   chat_message(manager_chat_id, purchase_chat_id, chat_room_type) -> chat_message.chat_room_id
--
-- 운영(prod)은 ddl-auto: update 라서 새 테이블은 자동으로 생기지만, 기존 행을 옮기지 못하고
-- NOT NULL FK 컬럼 추가(chat_message.chat_room_id 등)가 기존 데이터 때문에 실패한다.
-- 그래서 이 스크립트는 "새 버전을 띄우기 전에" 실행한다.
--
-- 실행 순서
--   0) DB 백업 (mysqldump)
--   1) 백엔드 중지 (채팅이 들어오지 않게)
--   2) 아래 [1] ~ [7] 순서대로 실행, [8] 검증 쿼리로 건수 확인
--   3) 새 버전 배포
--   4) 문제가 없으면 [9] 로 예전 테이블 정리 (선택)
--
-- 주의: 공동구매 채팅방 id 가 바뀐다 (chat_room 으로 새로 번호가 매겨짐).
--       알림(notification.target_id)은 [7] 에서 같이 바꾼다.

-- FK 이름은 Hibernate 가 랜덤으로 만들었기 때문에 information_schema 에서 찾아서 지운다
-- 사용: SET @t='테이블', @c='컬럼'; 후 아래 4줄 실행
-- SET @fk = (SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
--            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = @c
--              AND REFERENCED_TABLE_NAME IS NOT NULL LIMIT 1);
-- SET @sql = IFNULL(CONCAT('ALTER TABLE ', @t, ' DROP FOREIGN KEY ', @fk), 'SELECT 1');
-- PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- [1] chat_room 생성 + 기존 두 방 테이블 이관 (legacy_id 는 이관용 임시 컬럼)
CREATE TABLE chat_room (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    type            ENUM ('MANAGER', 'PURCHASE') NOT NULL,
    fridge_id       BIGINT NOT NULL,
    last_message_at DATETIME(6),
    created_time    DATETIME(6) NOT NULL,
    legacy_id       BIGINT,
    PRIMARY KEY (id),
    KEY idx_chat_room_fridge_type (fridge_id, type),
    CONSTRAINT fk_chat_room_fridge FOREIGN KEY (fridge_id) REFERENCES fridge (id)
) ENGINE = InnoDB;

INSERT INTO chat_room (type, fridge_id, last_message_at, created_time, legacy_id)
SELECT 'MANAGER', mc.fridge_id, mc.last_message_at, COALESCE(mc.last_message_at, NOW(6)), mc.id
FROM manager_chat_room mc;

INSERT INTO chat_room (type, fridge_id, last_message_at, created_time, legacy_id)
SELECT 'PURCHASE', pc.fridge_id, pc.last_message_at, pc.created_time, pc.id
FROM purchase_chat_room pc;


-- [2] purchase_post 생성 + 공동구매 게시글 정보 이관
CREATE TABLE purchase_post (
    id               BIGINT NOT NULL AUTO_INCREMENT,
    chat_room_id     BIGINT NOT NULL,
    creator_id       UUID NOT NULL,
    title            VARCHAR(255) NOT NULL,
    contents         TEXT,
    product_url      VARCHAR(255),
    max_participants INT NOT NULL,
    join_status      ENUM ('CLOSE', 'OPEN') NOT NULL,
    is_active        BIT NOT NULL,
    created_time     DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_purchase_post_chat_room (chat_room_id),
    CONSTRAINT fk_purchase_post_chat_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id),
    CONSTRAINT fk_purchase_post_creator FOREIGN KEY (creator_id) REFERENCES member (id)
) ENGINE = InnoDB;

INSERT INTO purchase_post (chat_room_id, creator_id, title, contents, product_url, max_participants, join_status, is_active, created_time)
SELECT cr.id, pc.creator_id, pc.title, pc.contents, pc.product_url, pc.max_participants, pc.join_status, pc.is_active, pc.created_time
FROM purchase_chat_room pc
JOIN chat_room cr ON cr.type = 'PURCHASE' AND cr.legacy_id = pc.id;


-- [3] chat_message: chat_room_id 로 연결, message_type 추가
ALTER TABLE chat_message
    ADD COLUMN chat_room_id BIGINT NULL,
    ADD COLUMN message_type ENUM ('IMAGE', 'SYSTEM', 'TEXT') NOT NULL DEFAULT 'TEXT';

UPDATE chat_message m
JOIN chat_room cr ON cr.type = 'MANAGER' AND cr.legacy_id = m.manager_chat_id
SET m.chat_room_id = cr.id
WHERE m.chat_room_type = 'MANAGER';

UPDATE chat_message m
JOIN chat_room cr ON cr.type = 'PURCHASE' AND cr.legacy_id = m.purchase_chat_id
SET m.chat_room_id = cr.id
WHERE m.chat_room_type = 'PURCHASE';

-- 방을 찾지 못한 메시지 (0이어야 정상. 있으면 확인 후 진행)
SELECT COUNT(*) AS orphan_messages FROM chat_message WHERE chat_room_id IS NULL;

UPDATE chat_message m SET m.message_type = 'IMAGE'
WHERE EXISTS (SELECT 1 FROM chat_message_image i WHERE i.chat_massage_id = m.id);
UPDATE chat_message SET is_deleted = 0 WHERE is_deleted IS NULL;

SET @t = 'chat_message', @c = 'manager_chat_id';
SET @fk = (SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = @c AND REFERENCED_TABLE_NAME IS NOT NULL LIMIT 1);
SET @sql = IFNULL(CONCAT('ALTER TABLE ', @t, ' DROP FOREIGN KEY ', @fk), 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @t = 'chat_message', @c = 'purchase_chat_id';
SET @fk = (SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = @c AND REFERENCED_TABLE_NAME IS NOT NULL LIMIT 1);
SET @sql = IFNULL(CONCAT('ALTER TABLE ', @t, ' DROP FOREIGN KEY ', @fk), 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

ALTER TABLE chat_message
    DROP COLUMN manager_chat_id,
    DROP COLUMN purchase_chat_id,
    DROP COLUMN chat_room_type,
    MODIFY chat_room_id BIGINT NOT NULL,
    MODIFY sender_id UUID NULL,
    MODIFY contents TEXT,
    MODIFY is_deleted BIT NOT NULL,
    ADD INDEX idx_chat_message_room_id (chat_room_id, id),
    ADD CONSTRAINT fk_chat_message_chat_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id);


-- [4] chat_message_image: FK 컬럼명 오타 수정 (chat_massage_id -> chat_message_id) + sort_order
SET @t = 'chat_message_image', @c = 'chat_massage_id';
SET @fk = (SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = @c AND REFERENCED_TABLE_NAME IS NOT NULL LIMIT 1);
SET @sql = IFNULL(CONCAT('ALTER TABLE ', @t, ' DROP FOREIGN KEY ', @fk), 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

ALTER TABLE chat_message_image
    CHANGE chat_massage_id chat_message_id BIGINT NOT NULL,
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0,
    ADD CONSTRAINT fk_chat_message_image_message FOREIGN KEY (chat_message_id) REFERENCES chat_message (id);

UPDATE chat_message_image i
JOIN (SELECT id, ROW_NUMBER() OVER (PARTITION BY chat_message_id ORDER BY id) - 1 AS rn FROM chat_message_image) x ON x.id = i.id
SET i.sort_order = x.rn;


-- [5] chat_participant: 모든 방에서 사용, 역할 + 읽음 커서 추가
ALTER TABLE chat_participant
    ADD COLUMN chat_room_id BIGINT NULL,
    ADD COLUMN role ENUM ('MANAGER', 'MEMBER') NOT NULL DEFAULT 'MEMBER',
    ADD COLUMN last_read_message_id BIGINT NOT NULL DEFAULT 0,
    MODIFY purchase_chat_room_id BIGINT NULL;

-- 공동구매 방 참여자
UPDATE chat_participant p
JOIN chat_room cr ON cr.type = 'PURCHASE' AND cr.legacy_id = p.purchase_chat_room_id
SET p.chat_room_id = cr.id;

-- 1대1 방: 방 주인(MEMBER)
INSERT INTO chat_participant (chat_room_id, member_id, role, last_read_message_id, joined_at)
SELECT cr.id, mc.member_id, 'MEMBER', 0, cr.created_time
FROM manager_chat_room mc
JOIN chat_room cr ON cr.type = 'MANAGER' AND cr.legacy_id = mc.id;

-- 1대1 방: 현재 냉장고 관리자(MANAGER). 관리자 본인 방에는 넣지 않는다
INSERT INTO chat_participant (chat_room_id, member_id, role, last_read_message_id, joined_at)
SELECT cr.id, fm.member_id, 'MANAGER', 0, cr.created_time
FROM manager_chat_room mc
JOIN chat_room cr ON cr.type = 'MANAGER' AND cr.legacy_id = mc.id
JOIN fridge_member fm ON fm.fridge_id = mc.fridge_id AND fm.type = 'MANAGER'
WHERE fm.member_id <> mc.member_id;

-- 읽음 커서: 가장 오래된 안 읽은 메시지 바로 앞. 안 읽은 게 없으면 방의 마지막 메시지
UPDATE chat_participant p
JOIN chat_room cr ON cr.id = p.chat_room_id
SET p.last_read_message_id = COALESCE(
        (SELECT MIN(r.chat_message_id) - 1 FROM is_read r
         WHERE r.member_id = p.member_id AND r.chat_room_type = cr.type AND r.room_id = cr.legacy_id AND r.is_read = 0),
        (SELECT MAX(m.id) FROM chat_message m WHERE m.chat_room_id = cr.id),
        0);

SET @t = 'chat_participant', @c = 'purchase_chat_room_id';
SET @fk = (SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = @t AND COLUMN_NAME = @c AND REFERENCED_TABLE_NAME IS NOT NULL LIMIT 1);
SET @sql = IFNULL(CONCAT('ALTER TABLE ', @t, ' DROP FOREIGN KEY ', @fk), 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

ALTER TABLE chat_participant
    DROP COLUMN purchase_chat_room_id,
    MODIFY chat_room_id BIGINT NOT NULL,
    ADD CONSTRAINT uk_chat_participant_room_member UNIQUE (chat_room_id, member_id),
    ADD CONSTRAINT fk_chat_participant_chat_room FOREIGN KEY (chat_room_id) REFERENCES chat_room (id);


-- [6] (확인용) 1대1 방 중 관리자 참여자가 없는 방 = 관리자 본인 방이어야 한다
SELECT cr.id, cr.fridge_id
FROM chat_room cr
WHERE cr.type = 'MANAGER'
  AND NOT EXISTS (SELECT 1 FROM chat_participant p WHERE p.chat_room_id = cr.id AND p.role = 'MANAGER');


-- [7] 알림의 공동구매 채팅방 id 를 새 chat_room id 로 변경
UPDATE notification n
JOIN chat_room cr ON cr.type = 'PURCHASE' AND cr.legacy_id = n.target_id
SET n.target_id = cr.id
WHERE n.target_type = 'CHAT';


-- [8] 검증: 각 쌍의 값이 같아야 한다
SELECT (SELECT COUNT(*) FROM manager_chat_room) AS legacy_manager_rooms,
       (SELECT COUNT(*) FROM chat_room WHERE type = 'MANAGER') AS manager_rooms;
SELECT (SELECT COUNT(*) FROM purchase_chat_room) AS legacy_purchase_rooms,
       (SELECT COUNT(*) FROM chat_room WHERE type = 'PURCHASE') AS purchase_rooms,
       (SELECT COUNT(*) FROM purchase_post) AS purchase_posts;
SELECT (SELECT SUM(current_participants) FROM purchase_chat_room) AS legacy_purchase_participants,
       (SELECT COUNT(*) FROM chat_participant p JOIN chat_room cr ON cr.id = p.chat_room_id WHERE cr.type = 'PURCHASE') AS purchase_participants;


-- [9] 정리 (선택, 새 버전 확인 후)
-- DROP TABLE is_read;
-- DROP TABLE manager_chat_room;
-- DROP TABLE purchase_chat_room;
-- ALTER TABLE chat_room DROP COLUMN legacy_id;
