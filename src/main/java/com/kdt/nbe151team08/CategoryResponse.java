package com.kdt.nbe151team08;

/**
 * 카테고리 API 성공 응답 DTO.
 * 명세에 맞게 id, name만 포함합니다.
 */
public record CategoryResponse(Integer id, String name) {
}
