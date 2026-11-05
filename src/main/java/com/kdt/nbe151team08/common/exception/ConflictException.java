package com.kdt.nbe151team08.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 업무 조건 충돌(409) 예외.
 * 명세: code·message는 오류 표 값, fieldErrors는 항상 {}.
 *
 * <pre>
 * throw new ConflictException("CATEGORY_IN_USE", "메뉴가 등록된 카테고리는 삭제할 수 없습니다.");
 * </pre>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    private final String code;

    public ConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
