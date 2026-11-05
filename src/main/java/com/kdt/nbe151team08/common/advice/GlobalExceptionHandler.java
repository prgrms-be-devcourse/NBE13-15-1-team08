package com.kdt.nbe151team08.common.advice;

import com.kdt.nbe151team08.common.exception.ConflictException;
import com.kdt.nbe151team08.common.exception.ErrorResponse;
import com.kdt.nbe151team08.common.exception.NotFoundException;
import com.kdt.nbe151team08.common.exception.ValidationException;
import com.kdt.nbe151team08.reference.OutsideApiException;
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
 * 공통 예외 처리기.
 * 컨트롤러/서비스에서 던진 예외 → {@link ErrorResponse}(code, message, fieldErrors) JSON으로 변환함.
 * 영역별 핸들러가 ResponseEntity를 직접 만들지 않아도 응답 형식이 통일됨.
 *
 * <ul>
 *   <li>400 — ValidationException, @Valid, JSON 파싱, 타입 불일치 등</li>
 *   <li>404 — NotFoundException</li>
 *   <li>409 — ConflictException</li>
 *   <li>502/504 — OutsideApiException (RestClient 외부 호출)</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 404 — 리소스 없음 */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getCode(), e.getMessage(), Map.of()));
    }

    /**
     * RestClient 외부 조회 실패.
     * 502 OUTSIDE_API_FAILED / 504 OUTSIDE_API_TIMEOUT — fieldErrors 빈 객체.
     */
    @ExceptionHandler(OutsideApiException.class)
    public ResponseEntity<ErrorResponse> handleOutsideApi(OutsideApiException e) {
        return ResponseEntity.status(e.getStatus())
                .body(new ErrorResponse(e.getCode(), e.getMessage(), Map.of()));
    }

    /** 409 — 공통 업무 충돌 (CATEGORY_NAME_DUPLICATED, CATEGORY_IN_USE, MENU_IN_USE 등) */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getCode(), e.getMessage(), Map.of()));
    }

    /** 400 — 서비스 검증 오류 (field 있으면 fieldErrors에 넣고, 없으면 빈 객체) */
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

    /** 400 — DTO @Valid 실패 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", "입력값을 확인해 주세요.", errors));
    }

    /** 400 — JSON 문법·형식 오류 */
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

    /** 400 — page=abc 등 경로/쿼리 타입 불일치 */
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

    /** 400 — 메뉴 검색어 등 IllegalArgumentException */
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
