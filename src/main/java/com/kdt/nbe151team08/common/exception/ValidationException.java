package com.kdt.nbe151team08.common.exception;

/**
 * 입력값 검증에 실패했을 때 던지는 예외.
 * ApiExceptionAdvice에서 400으로 변환합니다.
 */
public class ValidationException extends RuntimeException {

    private final String field;

    public ValidationException(String message) {
        super(message);
        this.field = null;
    }

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField(){
        return field;
    }
}
