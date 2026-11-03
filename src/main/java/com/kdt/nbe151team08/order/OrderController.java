package com.kdt.nbe151team08.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 주문 목록 / 이메일별 주문: GET /api/v1/orders?email=user@example.com
    /*@GetMapping
    public List<OrderDto.Response> findAll(@RequestParam(required = false) String email) {
        return orderService.findAll(email);
    }*/

    // 주문 상세: GET /api/v1/orders/{orderId}
    @GetMapping("/{orderId}")
    public OrderDto.Response findById(@PathVariable Integer orderId) {
        return orderService.findById(orderId);
    }

    // 주문 접수: POST /api/v1/orders
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto.Response create(@RequestBody OrderDto.CreateRequest request) {
        return orderService.create(request);
    }

    // 주문 취소: POST /api/v1/orders/{orderId}/cancel
    @PostMapping("/{orderId}/cancel")
    public OrderDto.Response cancel(@PathVariable Integer orderId) {
        return orderService.cancel(orderId);
    }

    // 재주문: POST /api/v1/orders/{orderId}/reorder
    @PostMapping("/{orderId}/reorder")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto.Response reorder(@PathVariable Integer orderId) {
        return orderService.reorder(orderId);
    }

    // 주문 메모 수정: PUT /api/v1/orders/{orderId}/memo
    @PutMapping("/{orderId}/memo")
    public OrderDto.Response updateMemo(@PathVariable Integer orderId,
                                        @RequestBody OrderDto.MemoUpdateRequest request) {
        return orderService.updateMemo(orderId, request);
    }
}
