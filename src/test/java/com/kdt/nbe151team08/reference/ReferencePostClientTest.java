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

    @BeforeEach
    void setUp() throws IOException {
        this.server = new MockWebServer();
        this.server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        this.server.shutdown();
    }

    @Test
    void success() {
        this.server.enqueue(json(200, "{\"id\":1,\"userId\":1,\"title\":\"제목\",\"body\":\"본문\"}"));

        ReferencePostResponse res = client(this.server.url("/").toString()).getPost(1);

        assertEquals(1, res.id());
        assertEquals(1, res.userId());
        assertEquals("제목", res.title());
        assertEquals("본문", res.body());
    }

    @Test
    void notFound_404() {
        this.server.enqueue(new MockResponse().setResponseCode(404));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> client(this.server.url("/").toString()).getPost(999));

        assertEquals("REFERENCE_POST_NOT_FOUND", ex.getCode());
    }

    @Test
    void serverError_502() {
        this.server.enqueue(new MockResponse().setResponseCode(500));

        OutsideApiException ex = assertThrows(OutsideApiException.class,
                () -> client(this.server.url("/").toString()).getPost(1));

        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatus());
        assertEquals("OUTSIDE_API_FAILED", ex.getCode());
    }

    @Test
    void connectionRefused_502() throws IOException {
        int port = freePort();

        OutsideApiException ex = assertThrows(OutsideApiException.class,
                () -> client("http://127.0.0.1:" + port).getPost(1));

        assertEquals("OUTSIDE_API_FAILED", ex.getCode());
    }

    @Test
    void connectTimeout_504() {
        OutsideApiException ex = assertThrows(OutsideApiException.class,
                () -> client("http://172.31.255.255:81", Duration.ofMillis(200), Duration.ofMillis(200)).getPost(1));

        assertEquals("OUTSIDE_API_TIMEOUT", ex.getCode());
    }

    @Test
    void readTimeout_504() {
        this.server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        OutsideApiException ex = assertThrows(OutsideApiException.class,
                () -> client(this.server.url("/").toString(), Duration.ofMillis(500), Duration.ofMillis(300)).getPost(1));

        assertEquals("OUTSIDE_API_TIMEOUT", ex.getCode());
    }

    private static ReferencePostClient client(String baseUrl) {
        return client(baseUrl, Duration.ofSeconds(1), Duration.ofSeconds(1));
    }

    private static ReferencePostClient client(String baseUrl, Duration connect, Duration read) {
        return new ReferencePostClient(baseUrl, connect, read);
    }

    private static MockResponse json(int code, String body) {
        return new MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            return socket.getLocalPort();
        }
    }
}
