package com.kdt.nbe151team08.category.dto;

/**
 * 카테고리 API 성공 응답 DTO.
 * 명세에 맞게 id, name만 포함함.
 */
public record CategoryResponse(int id, String name) {
}
