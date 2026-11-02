package com.kdt.nbe151team08;

/**
 * 리소스를 찾지 못했을 때 던지는 예외.
 * ApiExceptionAdvice에서 404로 변환합니다.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
