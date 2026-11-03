package com.kdt.nbe151team08.category.dto;

/**
 * 카테고리 등록/수정 요청 Body DTO.
 * 명세의 CategoryInput에 맞게 name만 받습니다.
 */
public record CategoryRequest(String name) {
}
