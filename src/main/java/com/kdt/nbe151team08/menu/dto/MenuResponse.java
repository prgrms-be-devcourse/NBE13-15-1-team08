package com.kdt.nbe151team08.menu.dto;

import com.kdt.nbe151team08.menu.entity.Menu;

import java.io.Serializable;

public record MenuResponse(
        Integer id, Integer categoryId, String name, Integer price, String description, Boolean available) implements Serializable {

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
