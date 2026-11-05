package com.kdt.nbe151team08.common.exception;

/**
 * 입력값 검증에 실패했을 때 던지는 예외.
 * 공통 예외 핸들러에서 400 · VALIDATION_ERROR · fieldErrors 로 변환

 * // 필드 지정 없이
 * throw new ValidationException("입력값을 확인해 주세요.");

 * // 특정 필드 오류
 * throw new ValidationException("name", "카테고리명은 1~30자로 입력해 주세요.");
 */
public class ValidationException extends RuntimeException {

    private final String field;
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
