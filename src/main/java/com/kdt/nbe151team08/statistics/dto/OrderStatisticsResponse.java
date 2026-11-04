package com.kdt.nbe151team08.statistics.dto;

import java.time.LocalDate;

// A17 (접수일별 주문 통계)
// OrderStatistics — 주문 통계 단일 객체
//  "date": "2026-11-03",
//          "orderCount": 2,
//          "placedOrderCount": 2,
//          "cancelledOrderCount": 0,
//          "totalQuantity": 4,
//          "totalAmount": 22000

public record OrderStatisticsResponse (
        LocalDate date,
        int orderCount,
        int placedOrderCount,
        int cancelledOrderCount,
        int totalQuantity,
        int totalAmount
) {
}

