# NBE13-15-1-team08

백엔드 1차 프로젝트 team08

## 대소문자 구분 (DB)

이미 만든 DB에는 팀 공통으로 아래를 실행합니다.

```bash
# src/main/resources/db/alter-case-sensitive.sql
```

신규 생성은 `schema.sql` 기준(`utf8mb4_bin`)을 사용합니다.

## API 수동 검증

- 파일: `requests/api-check.http`
- 정상 A01~A24, 오류 6건(400×2 / 404×1 / 409×3)
- IntelliJ HTTP Client에서 `baseUrl`·ID 변수 수정 후 실행

## 외부 게시글 조회 (RestClient)

- API: `GET /api/v1/reference/posts/{postId}`
- 기본 외부 주소: `https://jsonplaceholder.typicode.com` (`reference.base-url` / `REFERENCE_BASE_URL`로 변경 가능)
- 연결 대기 3초 · 응답 읽기 대기 5초
- 수동 확인: `requests/reference-posts.http`

### 자동 테스트 실행

```bash
# 모의 외부 서버(MockWebServer) — 성공·404·5xx·연결 거절·연결/읽기 시간 초과
./gradlew test --tests com.kdt.nbe151team08.reference.ReferencePostClientTest

# HTTP 응답 형식(400/404/502/504 ErrorResponse)
./gradlew test --tests com.kdt.nbe151team08.reference.ReferencePostControllerTest
```

실서비스(jsonplaceholder)에 의도적으로 오류를 내지 않습니다. 테스트는 `reference.base-url`을 Mock 주소로 바꿔 검증합니다.

## 기본 Controller 테스트

```bash
./gradlew test --tests com.kdt.nbe151team08.controller.BasicControllerTest
```

팀 기본 6개(정상 2 · 입력오류 2 · 404/409 2). `@WebMvcTest` + 모의 Service, DB 불필요.

## 배송일·취소 마감(13:59:59 / 14:00:00) 확인 방법

**실제 PC·서버 OS 시계는 바꾸지 않습니다.**  
기준 타임존은 **Asia/Seoul**입니다.

### 업무 규칙

| 구분 | 13:59:59 | 14:00:00 |
|------|----------|----------|
| **주문 접수** (`createdAt`) | 배송 대상일 = **당일** | 배송 대상일 = **다음 날** |
| **주문 취소** | 배송 대상일 14:00 **전**이고 `PLACED`이면 **200** 취소 | 배송 대상일 14:00 **이후**면 **409** `ORDER_NOT_CANCELLABLE` |

- 접수: `14:00` 미만 → 당일 배송, `14:00` 이상 → 익일 배송
- 취소: `now < shippingDate 의 14:00:00` 이고 `status == PLACED` 일 때만 허용

### 확인 방법 (OS 시계 변경 금지) — `java.time.Clock` 고정

앱·테스트에서 “지금”을 `Clock`으로 두고, 접수·취소 로직은 `LocalDateTime.now(clock)` / `LocalDate.now(clock)` 만 사용합니다.  
프로덕션 기본값은 `Clock.system(ZoneId.of("Asia/Seoul"))`, 확인 시에만 아래처럼 고정합니다.

```java
ZoneId seoul = ZoneId.of("Asia/Seoul");

// ① 13:59:59 — 당일 배송 · 취소 가능 구간
Clock at135959 = Clock.fixed(
        ZonedDateTime.of(2026, 10, 28, 13, 59, 59, 0, seoul).toInstant(),
        seoul);

// ② 14:00:00 — 익일 배송 · (당일 배송 주문은) 취소 마감
Clock at140000 = Clock.fixed(
        ZonedDateTime.of(2026, 10, 28, 14, 0, 0, 0, seoul).toInstant(),
        seoul);
```

### 기대 결과 체크리스트

**1) 배송일 (주문 접수)**

1. `Clock`을 `2026-10-28T13:59:59`로 고정한 뒤 `POST /api/v1/orders`  
   → 응답 `shippingDate` == **`2026-10-28`**
2. `Clock`을 `2026-10-28T14:00:00`로 고정한 뒤 동일 API  
   → 응답 `shippingDate` == **`2026-10-29`**

**2) 취소 마감**

1. `shippingDate = 2026-10-28`, `status = PLACED` 인 주문을 준비
2. `Clock` = `2026-10-28T13:59:59` → `POST .../cancel` → **200**, `status=CANCELLED`
3. (다른 PLACED 주문으로) `Clock` = `2026-10-28T14:00:00` → `POST .../cancel` → **409**, `code=ORDER_NOT_CANCELLABLE`

단위/통합 테스트에서 `@Bean Clock`을 위 고정 시각으로 교체하거나, 테스트 전용으로 `Clock`을 주입해 검증합니다.  
HTTP Client만 쓸 때도 **OS 시간을 돌리지 않고**, 서버가 사용하는 `Clock` 빈을 테스트 프로필·테스트 코드에서 고정하는 방식을 씁니다.
