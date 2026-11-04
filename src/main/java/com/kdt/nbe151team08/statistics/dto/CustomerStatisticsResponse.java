package com.kdt.nbe151team08.statistics.dto;

import java.time.LocalDate;

// A24 (접수일별 고객 통계)
// CustomerStatistics[] — 이메일별 통계
//{
//        "date": "2026-11-03",
//        "email": "buyer@example.com",
//        "orderCount": 2,
//        "placedOrderCount": 2,
//        "cancelledOrderCount": 0,
//        "totalQuantity": 4,
//        "totalAmount": 22000
//        }

public record CustomerStatisticsResponse(
        LocalDate date,
        String email,
        long orderCount,
        long placedOrderCount,
        long cancelledOrderCount,
        long totalQuantity,
        long totalAmount
) {
}
