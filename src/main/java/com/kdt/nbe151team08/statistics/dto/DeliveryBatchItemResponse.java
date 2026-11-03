package com.kdt.nbe151team08.statistics.dto;


// A14 (배송 묶음 items)
//"items": [
//        {
//        "menuId": 1,
//        "menuName": "Columbia Nariñó",
//        "unitPrice": 5000,
//        "quantity": 3,
//        "lineAmount": 15000
//        },
//        {
//        "menuId": 2,
//        "menuName": "Brazil Serra Do Caparaó",
//        "unitPrice": 7000,
//        "quantity": 1,
//        "lineAmount": 7000
//        }
//        ]

public record DeliveryBatchItemResponse(
        long menuId,
        String menuName,
        int unitPrice,
        long quantity,
        long lineAmount
) {
}