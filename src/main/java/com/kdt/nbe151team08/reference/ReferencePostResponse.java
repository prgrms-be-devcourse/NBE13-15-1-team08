package com.kdt.nbe151team08.reference;

/**
 * 외부 게시글 조회 성공 응답 DTO.
 *
 * <p>필드 의미:
 * <ul>
 *   <li>{@code id}, {@code userId} — 정수</li>
 *   <li>{@code title}, {@code body} — 문자열</li>
 * </ul>
 * 값은 외부 서버 JSON을 매핑한 것이며, 앱에서 고정값을 넣지 않습니다.
 */
public record ReferencePostResponse(int id, int userId, String title, String body) {
}
