package com.kdt.nbe151team08.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 업무 조건 충돌(409) 예외.
 * 입력 형식은 맞지만, 현재 상태와 맞지 않아 처리할 수 없을 때 사용함.
 *
 * <p>예: 주문에 쓰인 메뉴 삭제(MENU_IN_USE), 취소 마감 후 취소(ORDER_NOT_CANCELLABLE),
 * 판매 중지 메뉴로 주문(MENU_UNAVAILABLE) 등.
 *
 * <p>GlobalExceptionHandler#handleConflict 에서
 * HTTP 409 · {@link ErrorResponse}(code, message, fieldErrors={}) 로 변환함.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    /** ErrorResponse.code 에 들어갈 업무 코드 */
    private final String code;

    public ConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
