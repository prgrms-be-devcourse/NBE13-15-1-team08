-- =============================================================================
-- kdt-team08 통합 초기화 스크립트
-- 포함: 테이블 DDL · 초기 데이터(카테고리/메뉴) · 테스트용 주문 데이터
--
-- 실행 예:
--   docker cp src/main/resources/db/init.sql kdt-mysql:/tmp/init.sql
--   docker exec kdt-mysql mysql -uroot -pkdtpass --default-character-set=utf8mb4 -e "source /tmp/init.sql"
--   또는:
--   docker exec -i kdt-mysql mysql -uroot -pkdtpass --default-character-set=utf8mb4 < src/main/resources/db/init.sql
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `kdt-team08`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `kdt-team08`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `order_item`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `menu`;
DROP TABLE IF EXISTS `category`;

SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------------------------
-- 1. 테이블 구성 (DDL)
-- -----------------------------------------------------------------------------

CREATE TABLE `category` (
  `category_id` INT          NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(30)  NOT NULL,
  `description` VARCHAR(300) NULL,
  `available`   TINYINT(1)   NULL DEFAULT 1,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_category_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `menu` (
  `menu_id`     INT          NOT NULL AUTO_INCREMENT,
  `category_id` INT          NOT NULL,
  `menu_name`   VARCHAR(100) NOT NULL,
  `unit_price`  INT          NOT NULL,
  `description` VARCHAR(300) NULL,
  `available`   TINYINT(1)   NOT NULL DEFAULT 1,
  PRIMARY KEY (`menu_id`),
  KEY `idx_menu_category_id` (`category_id`),
  CONSTRAINT `fk_menu_category`
    FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `orders` (
  `id`            INT          NOT NULL AUTO_INCREMENT,
  `email`         VARCHAR(100) NOT NULL,
  `address`       VARCHAR(200) NULL,
  `postcode`      VARCHAR(5)   NULL,
  `shipping_date` DATE         NULL,
  `created_at`    DATETIME     NULL,
  `cancelled_at`  DATETIME     NULL,
  `total_amount`  INT          NULL,
  `status`        ENUM('PLACED', 'CANCELLED') NULL,
  `memo`          VARCHAR(200) NULL,
  PRIMARY KEY (`id`),
  KEY `idx_orders_email` (`email`),
  KEY `idx_orders_created_at` (`created_at`),
  KEY `idx_orders_shipping_date` (`shipping_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `order_item` (
  `id`          INT          NOT NULL AUTO_INCREMENT,
  `order_id`    INT          NOT NULL,
  `menu_id`     INT          NOT NULL,
  `menu_name`   VARCHAR(100) NULL,
  `unit_price`  INT          NULL,
  `quantity`    INT          NULL,
  `line_amount` INT          NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_item_order_id` (`order_id`),
  KEY `idx_order_item_menu_id` (`menu_id`),
  CONSTRAINT `fk_order_item_orders`
    FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. 초기 데이터 (카테고리 6 · 메뉴 18, 카테고리별 균등 3개)
--    카테고리별 마지막 메뉴 1개는 판매중지(available=FALSE)
-- -----------------------------------------------------------------------------

INSERT INTO `category` (`name`, `description`, `available`) VALUES
('싱글오리진', '단일 원산지 원두', TRUE),
('블렌드', '여러 원산지를 혼합한 원두', TRUE),
('아프리카', '에티오피아·케냐·르완다 등 아프리카 원두', TRUE),
('중남미', '과테말라·코스타리카·파나마 등 중남미 원두', TRUE),
('아시아', '인도네시아·인도 등 아시아 원두', TRUE),
('디카페인', '카페인 제거 원두', TRUE);

INSERT INTO `menu` (`category_id`, `menu_name`, `unit_price`, `description`, `available`) VALUES
-- 싱글오리진 (1)
(1, 'Columbia Nariñó', 5000, '콜롬비아 나리뇨 원두를 담은 커피빈 패키지입니다.', TRUE),
(1, 'Brazil Serra Do Caparaó', 7000, '브라질 세하 두 카파라오 원두를 담은 커피빈 패키지입니다.', TRUE),
(1, 'Kenya AA Nyeri', 12000, '케냐 니에리 AA 등급 원두 패키지입니다.', FALSE),
-- 블렌드 (2)
(2, 'Columbia Quindío (White Wine Extended Fermentation)', 9000, '콜롬비아 킨디오 원두를 담은 커피빈 패키지입니다.', TRUE),
(2, 'Ethiopia Sidamo', 11000, '에티오피아 시다모 원두를 담은 커피빈 패키지입니다.', TRUE),
(2, 'House Breakfast Blend', 6500, '아침용 하우스 블렌드 원두 패키지입니다.', FALSE),
-- 아프리카 (3)
(3, 'Ethiopia Yirgacheffe', 11500, '에티오피아 예가체프 원두 패키지입니다.', TRUE),
(3, 'Ethiopia Guji Hambela', 12500, '에티오피아 구지 함벨라 원두 패키지입니다.', TRUE),
(3, 'Rwanda Nyamasheke', 10500, '르완다 냐마셰케 원두 패키지입니다.', FALSE),
-- 중남미 (4)
(4, 'Guatemala Antigua', 9200, '과테말라 안티구아 원두 패키지입니다.', TRUE),
(4, 'Costa Rica Tarrazú', 11000, '코스타리카 타라주 원두 패키지입니다.', TRUE),
(4, 'Panama Boquete', 13500, '파나마 보케테 원두 패키지입니다.', FALSE),
-- 아시아 (5)
(5, 'Indonesia Sumatra Mandheling', 8500, '인도네시아 수마트라 만델링 원두 패키지입니다.', TRUE),
(5, 'Indonesia Java Preanger', 9200, '인도네시아 자바 프리앙어 원두 패키지입니다.', TRUE),
(5, 'India Monsooned Malabar', 8800, '인도 몬순 말라바르 원두 패키지입니다.', FALSE),
-- 디카페인 (6)
(6, 'Colombia Decaf Sugarcane', 9000, '콜롬비아 슈가케인 디카페인 원두 패키지입니다.', TRUE),
(6, 'Ethiopia Decaf', 10000, '에티오피아 디카페인 원두 패키지입니다.', TRUE),
(6, 'Brazil Decaf Cerrado', 7500, '브라질 세라도 디카페인 원두 패키지입니다.', FALSE);

-- -----------------------------------------------------------------------------
-- 3. 테스트용 주문 데이터 (주문 17건 · 접수/취소 혼합)
--    날짜 기준: 2026-11-04 · 2026-11-05
-- -----------------------------------------------------------------------------

SET @m1  = (SELECT menu_id FROM menu WHERE menu_name = 'Columbia Nariñó' LIMIT 1);
SET @m2  = (SELECT menu_id FROM menu WHERE menu_name = 'Brazil Serra Do Caparaó' LIMIT 1);
SET @m3  = (SELECT menu_id FROM menu WHERE menu_name = 'Columbia Quindío (White Wine Extended Fermentation)' LIMIT 1);
SET @m4  = (SELECT menu_id FROM menu WHERE menu_name = 'Ethiopia Sidamo' LIMIT 1);
SET @m7  = (SELECT menu_id FROM menu WHERE menu_name = 'Ethiopia Yirgacheffe' LIMIT 1);
SET @m10 = (SELECT menu_id FROM menu WHERE menu_name = 'Guatemala Antigua' LIMIT 1);

START TRANSACTION;

-- 주문 1: 2026-11-04 13:00:00 접수 → 2026-11-04 배송 / PLACED / 총 17,000원
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('buyer@example.com', '서울시 실습로 10 101호', '01234', '2026-11-04', '2026-11-04 13:00:00', NULL, 17000, 'PLACED', '');
SET @o1 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@o1, @m1, 'Columbia Nariñó', 5000, 2, 10000),
    (@o1, @m2, 'Brazil Serra Do Caparaó', 7000, 1, 7000);

-- 주문 2: 2026-11-04 13:20:00 접수 → 2026-11-04 배송 / PLACED / 총 5,000원
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('buyer@example.com', '서울시 실습로 10 101호', '01234', '2026-11-04', '2026-11-04 13:20:00', NULL, 5000, 'PLACED', '추가 주문');
SET @o2 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@o2, @m1, 'Columbia Nariñó', 5000, 1, 5000);

-- 주문 3: 2026-11-04 13:40:00 접수 → 2026-11-04 배송 / CANCELLED / 총 11,000원
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('buyer@example.com', '서울시 실습로 10 101호', '01234', '2026-11-04', '2026-11-04 13:40:00', '2026-11-04 13:45:00', 11000, 'CANCELLED', '주문 취소 요청');
SET @o3 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@o3, @m4, 'Ethiopia Sidamo', 11000, 1, 11000);

-- 주문 4: 2026-11-04 14:00:00 접수 → 2026-11-05 배송 / PLACED / 총 9,000원
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('guest@example.com', '서울시 게스트로 20 202호', '04567', '2026-11-05', '2026-11-04 14:00:00', NULL, 9000, 'PLACED', '');
SET @o4 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@o4, @m3, 'Columbia Quindío (White Wine Extended Fermentation)', 9000, 1, 9000);

-- 주문 5: 2026-11-04 15:00:00 접수 → 2026-11-05 배송 / PLACED / 총 16,000원
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('buyer@example.com', '서울시 실습로 10 101호', '01234', '2026-11-05', '2026-11-04 15:00:00', NULL, 16000, 'PLACED', '');
SET @o5 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@o5, @m2, 'Brazil Serra Do Caparaó', 7000, 1, 7000),
    (@o5, @m3, 'Columbia Quindío (White Wine Extended Fermentation)', 9000, 1, 9000);

-- 주문 6: 2026-11-05 09:00:00 접수 → 2026-11-05 배송 / PLACED / 총 22,000원
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('buyer@example.com', '서울시 실습로 10 101호', '01234', '2026-11-05', '2026-11-05 09:00:00', NULL, 22000, 'PLACED', '');
SET @o6 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@o6, @m4, 'Ethiopia Sidamo', 11000, 2, 22000);

-- 주문 7: 2026-11-05 11:00:00 접수 → 2026-11-05 배송 / CANCELLED / 총 9,000원
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('cancelonly@example.com', '서울시 테스트로 8', '04501', '2026-11-05', '2026-11-05 11:00:00', '2026-11-05 11:10:00', 9000, 'CANCELLED', '');
SET @o7 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@o7, @m3, 'Columbia Quindío (White Wine Extended Fermentation)', 9000, 1, 9000);

-- 주문 8: 2026-11-05 12:40:00 접수 → 2026-11-05 배송 / PLACED / 총 15,000원
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('guest@example.com', '서울시 게스트로 20 202호', '04567', '2026-11-05', '2026-11-05 12:40:00', NULL, 15000, 'PLACED', '');
SET @o8 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@o8, @m1, 'Columbia Nariñó', 5000, 3, 15000);

-- 주문 9: 2026-11-05 13:59:59 접수 → 2026-11-05 배송 / PLACED / 총 7,000원
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('new@example.com', '서울시 새봄길 7', '06789', '2026-11-05', '2026-11-05 13:59:59', NULL, 7000, 'PLACED', '');
SET @o9 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@o9, @m2, 'Brazil Serra Do Caparaó', 7000, 1, 7000);

-- 주문 10: 2026-11-05 14:00:00 접수 → 2026-11-06 배송 / PLACED / 총 18,000원
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('new@example.com', '서울시 새봄길 7', '06789', '2026-11-06', '2026-11-05 14:00:00', NULL, 18000, 'PLACED', '');
SET @o10 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@o10, @m3, 'Columbia Quindío (White Wine Extended Fermentation)', 9000, 2, 18000);

-- 주문 11: 2026-11-05 11:30:00 접수 → 2026-11-05 배송 / CANCELLED / 총 14,000원
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('guest@example.com', '서울시 게스트로 20 202호', '04567', '2026-11-05', '2026-11-05 11:30:00', '2026-11-05 11:45:00', 14000, 'CANCELLED', '');
SET @o11 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@o11, @m2, 'Brazil Serra Do Caparaó', 7000, 2, 14000);

-- 주문 12: 2026-11-05 12:00:00 접수 → 2026-11-05 배송 / PLACED / 총 21,000원
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('buyer@example.com', '서울시 실습로 10 101호', '01234', '2026-11-05', '2026-11-05 12:00:00', NULL, 21000, 'PLACED', '문 앞에 놓아주세요');
SET @o12 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@o12, @m4, 'Ethiopia Sidamo', 11000, 1, 11000),
    (@o12, @m1, 'Columbia Nariñó', 5000, 2, 10000);

-- 주문 13: 2026-11-05 14:00:01 접수 → 2026-11-06 배송 / PLACED / 총 18,000원
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('buyer@example.com', '서울시 실습로 10 101호', '01234', '2026-11-06', '2026-11-05 14:00:01', NULL, 18000, 'PLACED', '');
SET @o13 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@o13, @m3, 'Columbia Quindío (White Wine Extended Fermentation)', 9000, 2, 18000);

-- 취소 14: 2026-11-04 12:10 접수 → 12:20 취소
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('buyer@example.com', '서울시 실습로 10 101호', '01234', '2026-11-04', '2026-11-04 12:10:00', '2026-11-04 12:20:00', 10000, 'CANCELLED', '잘못 주문해서 취소');
SET @c1 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@c1, @m1, 'Columbia Nariñó', 5000, 2, 10000);

-- 취소 15: 2026-11-04 16:30 접수(익일배송) → 16:40 취소
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('guest@example.com', '서울시 게스트로 20 202호', '04567', '2026-11-05', '2026-11-04 16:30:00', '2026-11-04 16:40:00', 11500, 'CANCELLED', '');
SET @c2 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@c2, @m7, 'Ethiopia Yirgacheffe', 11500, 1, 11500);

-- 취소 16: 2026-11-05 10:15 접수 → 10:25 취소
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('new@example.com', '서울시 새봄길 7', '06789', '2026-11-05', '2026-11-05 10:15:00', '2026-11-05 10:25:00', 9200, 'CANCELLED', '배송지 변경으로 재주문 예정');
SET @c3 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@c3, @m10, 'Guatemala Antigua', 9200, 1, 9200);

-- 취소 17: 2026-11-05 13:10 접수 → 13:20 취소
INSERT INTO orders (email, address, postcode, shipping_date, created_at, cancelled_at, total_amount, status, memo)
VALUES ('buyer@example.com', '서울시 실습로 10 101호', '01234', '2026-11-05', '2026-11-05 13:10:00', '2026-11-05 13:20:00', 18000, 'CANCELLED', '');
SET @c4 = LAST_INSERT_ID();
INSERT INTO order_item (order_id, menu_id, menu_name, unit_price, quantity, line_amount) VALUES
    (@c4, @m2, 'Brazil Serra Do Caparaó', 7000, 1, 7000),
    (@c4, @m4, 'Ethiopia Sidamo', 11000, 1, 11000);

COMMIT;

-- -----------------------------------------------------------------------------
-- 4. 확인용 조회
-- -----------------------------------------------------------------------------

SELECT c.category_id, c.name, COUNT(m.menu_id) AS menu_count
FROM category c
LEFT JOIN menu m ON m.category_id = c.category_id
GROUP BY c.category_id, c.name
ORDER BY c.category_id;

SELECT menu_id, category_id, menu_name, unit_price, available
FROM menu
ORDER BY category_id, menu_id;

SELECT DATE(created_at) AS order_date, COUNT(*) AS order_count,
       SUM(status = 'CANCELLED') AS cancelled_count,
       SUM(CASE WHEN status = 'PLACED' THEN total_amount ELSE 0 END) AS placed_total_amount
FROM orders
WHERE created_at >= '2026-11-04 00:00:00' AND created_at < '2026-11-06 00:00:00'
GROUP BY DATE(created_at)
ORDER BY order_date;

SELECT status, COUNT(*) AS cnt FROM orders GROUP BY status;
