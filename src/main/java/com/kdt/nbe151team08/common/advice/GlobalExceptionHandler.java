package com.kdt.nbe151team08.common.advice;

import com.kdt.nbe151team08.category.exception.CategoryInUseException;
import com.kdt.nbe151team08.category.exception.CategoryNameDuplicatedException;
import com.kdt.nbe151team08.common.exception.ConflictException;
import com.kdt.nbe151team08.common.exception.ErrorResponse;
import com.kdt.nbe151team08.common.exception.NotFoundException;
import com.kdt.nbe151team08.common.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * API 예외를 ErrorResponse(code, message, fieldErrors)로 변환하는 공통 처리.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getCode(), e.getMessage(), Map.of()));
    }

    // 409 - 공통
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getCode(), e.getMessage(), Map.of()));
    }

    // 409 - 카테고리 이름 중복
    @ExceptionHandler(CategoryNameDuplicatedException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNameDuplicated(CategoryNameDuplicatedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CATEGORY_NAME_DUPLICATED", e.getMessage(), Map.of()));
    }

    // 409 - 카테고리 사용 중
    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<ErrorResponse> handleCategoryInUse(CategoryInUseException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CATEGORY_IN_USE", e.getMessage(), Map.of()));
    }

    // 400 - 서비스에서 던진 검증 오류
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e) {
        Map<String, String> fieldErrors = e.getField() == null || e.getFieldMessage() == null
                ? Map.of()
                : Map.of(e.getField(), e.getFieldMessage());

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        "VALIDATION_ERROR",
                        e.getMessage() == null || e.getMessage().isBlank()
                                ? "입력값을 확인해 주세요."
                                : e.getMessage(),
                        fieldErrors
                ));
    }

    // 400 - DTO @Valid 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", "입력값을 확인해 주세요.", errors));
    }

    // JSON 문법이나 형식 오류
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadJson(
            HttpMessageNotReadableException e) {

        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        "VALIDATION_ERROR",
                        "입력값을 확인해 주세요.",
                        Map.of()
                )
        );
    }

    // page=abc, size=abc 등 정수 변환 오류
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException e) {

        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        "VALIDATION_ERROR",
                        "입력값을 확인해 주세요.",
                        Map.of(e.getName(), "올바른 정수를 입력해 주세요.")
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        "VALIDATION_ERROR",
                        "입력값을 확인해 주세요.",
                        Map.of("keyword", e.getMessage() == null ? "입력값을 확인해 주세요." : e.getMessage())
                )
        );
    }
}
