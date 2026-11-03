package com.kdt.nbe151team08;

import java.time.LocalDate;
import java.util.List;

// A14 (배송 묶음)
//        {
//        "shippingDate": "2026-11-03",
//        "email": "buyer@example.com",
//        "address": "서울시 실습로 10 101호",
//        "postcode": "01234",
//        "orderIds": [
//        101,
//        102
//        ],
//        "orderCount": 2,
//        "totalQuantity": 4,
//        "totalAmount": 22000,
//        "items": []
//         }

public record DeliveryBatchResponse(
        LocalDate shippingDate,
        String email,
        String address,
        String postcode,
        List<Long> orderIds,
        long orderCount,
        long totalQuantity,
        long totalAmount,
        List<DeliveryBatchItemResponse> items
) {
}