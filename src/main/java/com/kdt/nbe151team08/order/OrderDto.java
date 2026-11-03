package com.kdt.nbe151team08.order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    // 주문 접수 요청: { "email", "address", "postcode", "items": [ { "menuId", "quantity" } ] }
    public record CreateRequest(String email, String address, String postcode,
                                List<ItemRequest> items) {
    }

    public record ItemRequest(Integer menuId, Integer quantity) {
    }

    public record MemoUpdateRequest(String memo) {
    }

    public record Response(Integer id, String email, String address, String postcode,
                           LocalDate shippingDate, LocalDateTime createdAt, LocalDateTime cancelledAt,
                           Integer totalAmount, OrderStatus status, String memo,
                           List<ItemResponse> items) {
        public static Response from(Order order) {
            return new Response(
                    order.getId(),
                    order.getEmail(),
                    order.getAddress(),
                    order.getPostcode(),
                    order.getShippingDate(),
                    order.getCreatedAt(),
                    order.getCancelledAt(),
                    order.getTotalAmount(),
                    order.getStatus(),
                    order.getMemo(),
                    order.getItems().stream().map(ItemResponse::from).toList()
            );
        }
    }

    public record ItemResponse(Integer menuId, String menuName, Integer unitPrice,
                               Integer quantity, Integer lineAmount) {
        public static ItemResponse from(OrderItem item) {
            return new ItemResponse(
                    item.getMenuId(),
                    item.getMenuName(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    item.getLineAmount()
            );
        }
    }
}
