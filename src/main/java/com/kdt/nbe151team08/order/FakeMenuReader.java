package com.kdt.nbe151team08.order;

import org.springframework.stereotype.Component;

/**
 * 메뉴 코드가 합쳐지기 전까지 사용하는 임시 구현.
 * TODO: 메뉴 기능이 main에 합쳐지면 실제 구현체로 교체하고 이 클래스는 삭제
 */
@Component
public class FakeMenuReader implements MenuReader {

    @Override
    public MenuInfo getMenu(Integer menuId) {
        return new MenuInfo(menuId, "테스트메뉴" + menuId, 5000, true);
    }
}
