package com.kdt.nbe151team08.reference;

import com.kdt.nbe151team08.common.exception.NotFoundException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * RestClient 외부 조회 자동 테스트.
 * MockWebServer로 성공·404·5xx·연결 거절·연결/읽기 시간 초과를 확인합니다.
 * (실제 jsonplaceholder에는 오류를 발생시키지 않습니다.)
 */
class ReferencePostClientTest {

    private MockWebServer server;

    /** 테스트마다 모의 외부 서버를 켠다. */
    @BeforeEach
    void setUp() throws IOException {
        this.server = new MockWebServer();
        this.server.start();
    }

    /** 테스트 후 모의 서버를 종료한다. */
    @AfterEach
    void tearDown() throws IOException {
        this.server.shutdown();
    }

    /** 성공: 외부 200 JSON을 id·userId·title·body로 매핑한다. */
    @Test
    void success() {
        this.server.enqueue(json(200, "{\"id\":1,\"userId\":1,\"title\":\"제목\",\"body\":\"본문\"}"));

        ReferencePostResponse res = client(this.server.url("/").toString()).getPost(1);

        assertEquals(1, res.id());
        assertEquals(1, res.userId());
        assertEquals("제목", res.title());
        assertEquals("본문", res.body());
    }

    /** 외부 404 → REFERENCE_POST_NOT_FOUND */
    @Test
    void notFound_404() {
        this.server.enqueue(new MockResponse().setResponseCode(404));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> client(this.server.url("/").toString()).getPost(999));

        assertEquals("REFERENCE_POST_NOT_FOUND", ex.getCode());
    }

    /** 외부 5xx → 502 OUTSIDE_API_FAILED */
    @Test
    void serverError_502() {
        this.server.enqueue(new MockResponse().setResponseCode(500));

        OutsideApiException ex = assertThrows(OutsideApiException.class,
                () -> client(this.server.url("/").toString()).getPost(1));

        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatus());
        assertEquals("OUTSIDE_API_FAILED", ex.getCode());
    }

    /** 연결 거절(닫힌 포트) → 502 OUTSIDE_API_FAILED */
    @Test
    void connectionRefused_502() throws IOException {
        int port = freePort();

        OutsideApiException ex = assertThrows(OutsideApiException.class,
                () -> client("http://127.0.0.1:" + port).getPost(1));

        assertEquals("OUTSIDE_API_FAILED", ex.getCode());
    }

    /** 연결 시간 초과 → 504 OUTSIDE_API_TIMEOUT */
    @Test
    void connectTimeout_504() {
        OutsideApiException ex = assertThrows(OutsideApiException.class,
                () -> client("http://172.31.255.255:81", Duration.ofMillis(200), Duration.ofMillis(200)).getPost(1));

        assertEquals("OUTSIDE_API_TIMEOUT", ex.getCode());
    }

    /** 응답 읽기 시간 초과 → 504 OUTSIDE_API_TIMEOUT */
    @Test
    void readTimeout_504() {
        this.server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        OutsideApiException ex = assertThrows(OutsideApiException.class,
                () -> client(this.server.url("/").toString(), Duration.ofMillis(500), Duration.ofMillis(300)).getPost(1));

        assertEquals("OUTSIDE_API_TIMEOUT", ex.getCode());
    }

    /** 기본 타임아웃(1초)으로 클라이언트 생성 */
    private static ReferencePostClient client(String baseUrl) {
        return client(baseUrl, Duration.ofSeconds(1), Duration.ofSeconds(1));
    }

    /** base-url·연결/읽기 타임아웃을 지정해 클라이언트 생성 (설정으로 주소 교체와 동일) */
    private static ReferencePostClient client(String baseUrl, Duration connect, Duration read) {
        return new ReferencePostClient(baseUrl, connect, read);
    }

    /** 모의 서버 JSON 응답 헬퍼 */
    private static MockResponse json(int code, String body) {
        return new MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    /** 사용 중이지 않은 로컬 포트 번호 (연결 거절 테스트용) */
    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            return socket.getLocalPort();
        }
    }
}
