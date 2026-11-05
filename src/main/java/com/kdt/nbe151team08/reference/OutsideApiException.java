package com.kdt.nbe151team08.reference;

import org.springframework.http.HttpStatus;

/**
 * 외부 서버 호출 실패 예외.
 *
 * <p>GlobalExceptionHandler에서 ErrorResponse(code, message, fieldErrors={})로 변환함.
 * <ul>
 *   <li>{@link #failed()} — 502 OUTSIDE_API_FAILED (5xx, 연결 거절, 도메인 오류 등)</li>
 *   <li>{@link #timeout()} — 504 OUTSIDE_API_TIMEOUT (연결/응답 읽기 시간 초과)</li>
 * </ul>
 */
public class OutsideApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public OutsideApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    /** 호출 실패·외부 오류 상태 → 502 (게이트웨이/중계 실패로 표현함) */
    public static OutsideApiException failed() {
        return new OutsideApiException(HttpStatus.BAD_GATEWAY, "OUTSIDE_API_FAILED", "외부 서버를 호출하지 못했습니다.");
    }

    /** 연결·읽기 시간 초과 → 504 (게이트웨이 타임아웃으로 표현함) */
    public static OutsideApiException timeout() {
        return new OutsideApiException(HttpStatus.GATEWAY_TIMEOUT, "OUTSIDE_API_TIMEOUT", "외부 서버의 응답 시간이 초과되었습니다.");
    }
}
