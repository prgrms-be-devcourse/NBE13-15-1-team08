package com.kdt.nbe151team08;

// A18 (접수일별 메뉴 통계)
//MenuStatistics[] — 메뉴별 통계
//{
//        "menuId": 1,
//        "menuName": "Columbia Nariñó",
//        "unitPrice": 5000,
//        "totalQuantity": 3,
//        "totalAmount": 15000
//        },
//        {
//        "menuId": 2,
//        "menuName": "Brazil Serra Do Caparaó",
//        "unitPrice": 7000,
//        "totalQuantity": 1,
//        "totalAmount": 7000
//        }

public record MenuStatisticsResponse(
        long menuId,
        String menuName,
        int unitPrice,
        long totalQuantity,
        long totalAmount
) {
}
