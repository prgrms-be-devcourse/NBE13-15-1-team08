package com.kdt.nbe151team08.menu.dto;

import com.kdt.nbe151team08.menu.entity.Menu;
public record MenuResponse(
        Integer id, Integer categoryId, String name, Integer price, String description, Boolean available) {

    public static MenuResponse from(Menu menu) {
        return new MenuResponse(
                menu.getMenuId(),
                menu.getCategoryId(),
                menu.getMenuName(),
                menu.getUnitPrice(),
                menu.getDescription(),
                menu.getAvailable()
        );
    }
}
