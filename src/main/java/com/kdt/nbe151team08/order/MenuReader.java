package com.kdt.nbe151team08.order;

/**
 * 주문 쪽에서 필요한 "메뉴 조회" 기능.
 * 메뉴 코드가 합쳐지면 MenuRepository를 사용하는 구현체를 만들고 FakeMenuReader를 삭제하면 됩니다.
 */
public interface MenuReader {

    /** 메뉴가 없으면 NotFoundException을 던집니다. */
    MenuInfo getMenu(Integer menuId);
}
