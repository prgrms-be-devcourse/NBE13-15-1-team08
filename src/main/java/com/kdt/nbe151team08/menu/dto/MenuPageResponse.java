package com.kdt.nbe151team08.menu.dto;

import java.util.List;

public record MenuPageResponse(
        List<MenuResponse> content,
        int number,
        int size,
        long totalElements,
        int totalPages) {

}
