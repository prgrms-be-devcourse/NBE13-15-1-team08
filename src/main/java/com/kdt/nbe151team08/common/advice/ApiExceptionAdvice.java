package com.kdt.nbe151team08.common.advice;

import com.kdt.nbe151team08.category.exception.CategoryInUseException;
import com.kdt.nbe151team08.category.exception.CategoryNameDuplicatedException;
import com.kdt.nbe151team08.common.exception.ApiError;
import com.kdt.nbe151team08.common.exception.NotFoundException;
import com.kdt.nbe151team08.common.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 예외를 HTTP 응답으로 변환하는 공통 처리.
 */
@RestControllerAdvice
public class ApiExceptionAdvice {

    /** NotFoundException → 404 · code는 예외에서 전달 */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(ex.getCode(), ex.getMessage()));
    }

    /** ValidationException → 400 · VALIDATION_ERROR */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("VALIDATION_ERROR", ex.getMessage()));
    }

    /** CategoryNameDuplicatedException → 409 · CATEGORY_NAME_DUPLICATED */
    @ExceptionHandler(CategoryNameDuplicatedException.class)
    public ResponseEntity<ApiError> handleNameDuplicated(CategoryNameDuplicatedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("CATEGORY_NAME_DUPLICATED", ex.getMessage()));
    }

    /** CategoryInUseException → 409 · CATEGORY_IN_USE */
    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<ApiError> handleInUse(CategoryInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("CATEGORY_IN_USE", ex.getMessage()));
    }
}
