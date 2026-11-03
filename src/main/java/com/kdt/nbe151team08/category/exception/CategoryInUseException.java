package com.kdt.nbe151team08.category.exception;

/**
 * 메뉴가 등록된 카테고리를 삭제하려 할 때 던지는 예외.
 * GlobalExceptionHandler에서 409로 변환합니다.
 */
public class CategoryInUseException extends RuntimeException {

    public CategoryInUseException(String message) {
        super(message);
    }
}
