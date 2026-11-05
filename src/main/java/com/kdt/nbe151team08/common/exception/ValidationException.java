package com.kdt.nbe151team08.common.exception;

/**
 * 입력값 검증 실패(400) 예외.
 * GlobalExceptionHandler#handleValidation 에서
 * HTTP 400 · code=VALIDATION_ERROR · fieldErrors 로 변환함.
 *
 * <pre>
 * // 필드 지정 없이 — fieldErrors = {}
 * throw new ValidationException("이메일은 필수입니다.");
 *
 * // 특정 필드 — fieldErrors = { "name": "..." }
 * throw new ValidationException("name", "카테고리명은 1~30자로 입력해 주세요.");
 * </pre>
 */
public class ValidationException extends RuntimeException {

    /** fieldErrors 키. 없으면 null → 빈 맵 */
    private final String field;

    /** fieldErrors 값. 없으면 null → 빈 맵 */
    private final String fieldMessage;

    /** 필드 지정 없이 메시지만 전달할 때 */
    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.fieldMessage = null;
    }

    /**
     * 특정 필드 오류일 때.
     * message는 명세 기본 문구, field/fieldMessage는 ErrorResponse.fieldErrors에 넣음.
     */
    public ValidationException(String field, String fieldMessage) {
        super("입력값을 확인해 주세요.");
        this.field = field;
        this.fieldMessage = fieldMessage;
    }

    public String getField() {
        return field;
    }

    public String getFieldMessage() {
        return fieldMessage;
    }
}
