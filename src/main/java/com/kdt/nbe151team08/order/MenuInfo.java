package com.kdt.nbe151team08.order;

/**
 * 주문에 필요한 메뉴 정보만 담은 값 객체.
 * Menu 엔티티 대신 이 타입을 사용해서 주문 코드가 메뉴 코드에 의존하지 않게 합니다.
 */
public record MenuInfo(Integer id, String name, int unitPrice, boolean available) {
}
