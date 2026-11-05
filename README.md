# NBE13-15-1-team08

백엔드 1차 프로젝트 team08: 살려주세요.

## 실행 전 준비

1. **MySQL** (`kdt-team08` DB, 타임존 `Asia/Seoul`)
2. **Redis** (`127.0.0.1:6379`) — 메뉴 목록 캐시용
3. DB 계정 설정

```bash
# 예시 파일을 복사한 뒤 username / password 입력
cp src/main/resources/application-local.yaml.example src/main/resources/application-local.yaml
```

## 앱 실행

- IntelliJ: 메인 애플리케이션 Run  
- API 주소: **`http://127.0.0.1:8080`**

## 프론트 · CORS

| 항목 | 값 |
|------|-----|
| API | `http://127.0.0.1:8080` |
| 프론트 Origin | **`http://127.0.0.1:8000`** (`@CrossOrigin` 허용) |


## 주문·시각 규칙 (요약)

- 주문 items: **1~10개**, menuId **중복 불가**, 수량 **1~20**
- 주소·우편번호: 주소 필수(최대 200자), 우편번호 **숫자 5자리**
- 시각: **Asia/Seoul** (`LocalDateTime.now(ZoneId.of("Asia/Seoul"))`)
- 접수 14:00 미만 → 당일 배송 / 14:00 이상 → 익일 배송
- 취소: 배송일 14:00 **전** + `PLACED`만 가능 (이후 → 409 `ORDER_NOT_CANCELLABLE`)

## API 검증 (팀 기본 · HTTP Client)

앱을 켠 뒤 IntelliJ에서 `.http` 파일을 실행합니다.

1. Spring Boot 실행 (`8080`)
2. `requests/api-check.http` 열기
3. 변수 맞추기 (`@baseUrl`, `@categoryId`, `@menuId`, `@orderId` 등)
4. 요청 왼쪽 **▶** 클릭
5. 정상 **A01→A24** 위→아래, 오류 **E01~E06**은 별도

| 파일 | 내용 |
|------|------|
| `requests/api-check.http` | 정상 A01~A24 · 오류 6건(400×2 / 404×1 / 409×3) |
| `requests/reference-posts.http` | 외부 게시글 조회 |

## 심화 구현 요약

| 기능 | 설명 |
|------|------|
| Redis 메뉴 캐시 | `GET /menus`, `GET /menus/available` — TTL 60초, 메뉴 변경 시 캐시 삭제 |
| 메뉴 페이징 | `GET /api/v1/menus/page?page=&size=` |
| RestClient | `GET /api/v1/reference/posts/{postId}` — 연결 3초 · 읽기 5초 · 기본 `jsonplaceholder` |

외부 주소 변경: `reference.base-url` / 환경변수 `REFERENCE_BASE_URL`

## 배송일·취소 마감(13:59:59 / 14:00:00) 확인 방법

**PC·서버 OS 시계는 바꾸지 않습니다.** 기준 타임존은 **Asia/Seoul**입니다.

### 업무 규칙

| 구분 | 13:59:59 | 14:00:00 |
|------|----------|----------|
| **주문 접수** | 배송일 = **당일** | 배송일 = **다음 날** |
| **주문 취소** | `PLACED`이면 **200** 취소 | **409** `ORDER_NOT_CANCELLABLE` |

### 구현 위치

| 동작 | 위치 |
|------|------|
| 접수·배송일 | `Order` 생성자 — `now(Asia/Seoul)`, `hour >= 14`면 익일 |
| 취소 마감 | `OrderService#cancel` — `shippingDate` 14:00과 서울 `now` 비교 |
| 취소 시각 | `Order#cancel` — `cancelledAt`도 서울 기준 |
