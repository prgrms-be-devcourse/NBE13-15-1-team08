package com.kdt.nbe151team08.common.exception;

import java.util.Map;

/**
 * API 에러 응답 공통 본문.
 * GlobalExceptionHandler가 예외를 이 형태로 변환함.
 *
 * <ul>
 *   <li>{@code code} — 에러 구분 코드 (예: VALIDATION_ERROR, MENU_UNAVAILABLE)</li>
 *   <li>{@code message} — 사용자/클라이언트용 메시지</li>
 *   <li>{@code fieldErrors} — 필드별 상세 (없으면 빈 맵 {})</li>
 * </ul>
 */
public record ErrorResponse(String code, String message, Map<String, String> fieldErrors) {
}
