package com.kdt.nbe151team08.common.exception;

/**
 * 리소스 없음(404) 예외.
 * 요청한 ID·대상이 DB에 없을 때 사용함.
 *
 * <p>예: CATEGORY_NOT_FOUND, MENU_NOT_FOUND, ORDER_NOT_FOUND, REFERENCE_POST_NOT_FOUND.
 *
 * <p>GlobalExceptionHandler#handleNotFound 에서
 * HTTP 404 · {@link ErrorResponse}(code, message, fieldErrors={}) 로 변환함.
 */
public class NotFoundException extends RuntimeException {

    /** ErrorResponse.code 에 들어갈 업무 코드 */
    private final String code;

    public NotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
