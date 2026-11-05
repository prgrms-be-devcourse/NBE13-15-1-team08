package com.kdt.nbe151team08.common.exception;

/**
 * 리소스 없음(404) 예외.
 * 명세: code·message는 오류 표 값, fieldErrors는 항상 {}.
 *
 * <pre>
 * throw new NotFoundException("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다.");
 * </pre>
 */
public class NotFoundException extends RuntimeException {

    private final String code;

    public NotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
