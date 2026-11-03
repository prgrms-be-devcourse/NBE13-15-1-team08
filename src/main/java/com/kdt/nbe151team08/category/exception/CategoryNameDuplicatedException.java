package com.kdt.nbe151team08.category.exception;

/**
 * 카테고리 이름이 중복될 때 던지는 예외.
 * ApiExceptionAdvice에서 409로 변환합니다.
 */
public class CategoryNameDuplicatedException extends RuntimeException {

    public CategoryNameDuplicatedException(String message) {
        super(message);
    }
}
