package com.kdt.nbe151team08.reference;

import com.kdt.nbe151team08.common.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * RestClient로 외부 게시글을 조회하는 클라이언트.
 *
 * <p>구현 기능:
 * <ul>
 *   <li>외부 {@code GET {baseUrl}/posts/{postId}} 호출 (기본: jsonplaceholder)</li>
 *   <li>base-url·타임아웃은 application.yaml {@code reference.*} 로 변경 가능 (테스트용 Mock 서버 주소 포함)</li>
 *   <li>연결 대기 최대 3초, 응답 읽기 대기 최대 5초</li>
 *   <li>외부 404 → {@code REFERENCE_POST_NOT_FOUND}</li>
 *   <li>외부 5xx·기타 오류 상태 → 502 {@code OUTSIDE_API_FAILED}</li>
 *   <li>연결 거절·도메인 오류 → 502 {@code OUTSIDE_API_FAILED}</li>
 *   <li>연결/읽기 시간 초과 → 504 {@code OUTSIDE_API_TIMEOUT}</li>
 * </ul>
 */
@Service
public class ReferencePostClient {

    private final RestClient restClient;

    /**
     * RestClient 생성.
     * connect-timeout / read-timeout 설정을 HttpClient·RequestFactory에 반영합니다.
     */
    public ReferencePostClient(
            @Value("${reference.base-url}") String baseUrl,
            @Value("${reference.connect-timeout}") Duration connectTimeout,
            @Value("${reference.read-timeout}") Duration readTimeout
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(readTimeout);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    /**
     * 외부 게시글 1건 조회.
     * 응답 JSON을 {@link ReferencePostResponse}로 변환하며, 고정값을 쓰지 않습니다.
     */
    public ReferencePostResponse getPost(int postId) {
        try {
            return this.restClient.get()
                    .uri("/posts/{postId}", postId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    // 외부 404
                    .onStatus(status -> status.value() == 404, (req, res) -> {
                        throw new NotFoundException("REFERENCE_POST_NOT_FOUND", "외부 게시글을 찾을 수 없습니다.");
                    })
                    // 외부 5xx 등 오류 상태
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw OutsideApiException.failed();
                    })
                    .body(ReferencePostResponse.class);
        } catch (NotFoundException | OutsideApiException e) {
            throw e;
        } catch (ResourceAccessException e) {
            // 네트워크 단 실패: 시간 초과면 504, 그 외(연결 거절·도메인 오류 등)는 502
            throw isTimeout(e) ? OutsideApiException.timeout() : OutsideApiException.failed();
        }
    }

    /** 예외 원인에 Timeout이 포함되면 읽기/연결 시간 초과로 판단합니다. */
    private boolean isTimeout(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            String name = t.getClass().getSimpleName();
            String message = t.getMessage() == null ? "" : t.getMessage().toLowerCase();
            if (name.contains("Timeout") || message.contains("timed out") || message.contains("timeout")) {
                return true;
            }
        }
        return false;
    }
}
