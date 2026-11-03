package com.kdt.nbe151team08.common.exception;

/**
 * API 실패 응답 Body.
 * 예: { "code": "CATEGORY_NOT_FOUND", "message": "카테고리를 찾을 수 없습니다." }
 */
public record ApiError(String code, String message) {
}
