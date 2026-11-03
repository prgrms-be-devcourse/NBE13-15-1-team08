package com.kdt.nbe151team08.order.dto;


import com.kdt.nbe151team08.order.entity.Order;
import com.kdt.nbe151team08.order.entity.OrderItem;
import com.kdt.nbe151team08.order.entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

public class OrderDto {

    // 주문 접수 요청: { "email", "address", "postcode", "items": [ { "menuId", "quantity" } ] }
    public record CreateRequest(String email, String address, String postcode,
                                List<ItemRequest> items) {
    }

    public record ItemRequest(Integer menuId, Integer quantity, Integer unitPrice) {
    }

    public record MemoUpdateRequest(String memo) {
    }

    public record Response(Integer id, String email, String address, String postcode,
                           LocalDate shippingDate, @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
                           OffsetDateTime createdAt, LocalDateTime cancelledAt,
                           Integer totalAmount, OrderStatus status, String memo,
                           List<ItemResponse> items) {
        public static Response from(Order order) {
            return new Response(
                    order.getId(),
                    order.getEmail(),
                    order.getAddress(),
                    order.getPostcode(),
                    order.getShippingDate(),
                    order.getCreatedAt() == null ? null
                            : order.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toOffsetDateTime(),
                    order.getCancelledAt(),
                    order.getTotalAmount(),
                    order.getStatus(),
                    order.getMemo(),
                    order.getItems().stream().map(ItemResponse::from).toList()
            );
        }
    }

    // 이메일별 주문 목록 응답: [ { "id", "createdAt", "shippingDate", "totalAmount", "status" } ]
    public record Summary(Integer id,
                          @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
                          OffsetDateTime createdAt,
                          LocalDate shippingDate,
                          Integer totalAmount,
                          OrderStatus status) {
        private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

        public static Summary from(Order order) {
            return new Summary(
                    order.getId(),
                    // DB의 LocalDateTime(서울 시간)에 +09:00 오프셋을 붙임
                    order.getCreatedAt() == null ? null : order.getCreatedAt().atZone(SEOUL).toOffsetDateTime(),
                    order.getShippingDate(),
                    order.getTotalAmount(),
                    order.getStatus()
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
