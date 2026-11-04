package com.kdt.nbe151team08.order.controller;


import com.kdt.nbe151team08.order.dto.OrderDto;
import com.kdt.nbe151team08.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 이메일별 주문: GET /api/v1/orders?email=user@example.com 200
    @GetMapping
    public List<OrderDto.Summary> findByEmail(@RequestParam(required = false) String email) {
        return orderService.findByEmail(email);
    }

    // 주문 상세: GET /api/v1/orders/{orderId} 200
    @GetMapping("/{orderId}")
    public OrderDto.Response findById(@PathVariable Integer orderId) {
        return orderService.findById(orderId);
    }

    // 주문 접수: POST /api/v1/orders 201
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto.Response create(@RequestBody OrderDto.CreateRequest request) {
        return orderService.create(request);
    }

    // 주문 취소: POST /api/v1/orders/{orderId}/cancel 200
    @PostMapping("/{orderId}/cancel")
    public OrderDto.Response cancel(@PathVariable Integer orderId) {
        return orderService.cancel(orderId);
    }

    // 재주문: POST /api/v1/orders/{orderId}/reorder 201
    @PostMapping("/{orderId}/reorder")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto.Response reorder(@PathVariable Integer orderId) {
        return orderService.reorder(orderId);
    }

    // 주문 메모 수정: PUT /api/v1/orders/{orderId}/memo 200
    @PutMapping("/{orderId}/memo")
    public OrderDto.Response updateMemo(@PathVariable Integer orderId,
                                        @RequestBody OrderDto.MemoUpdateRequest request) {
        return orderService.updateMemo(orderId, request);
    }
}
