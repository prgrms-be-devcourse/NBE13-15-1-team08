package com.kdt.nbe151team08.statistics.repository;

import com.kdt.nbe151team08.order.entity.Order;
import com.kdt.nbe151team08.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 통계 Repository
public interface StatisticsRepository extends JpaRepository<Order, Integer> {

// A17·A18·A24에서 사용 — "그날 하루 동안 접수된 주문" (취소 주문도 포함)
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

// A14에서 사용 — "그 배송일에 배송할 주문" (취소 주문은 제외)
    List<Order> findByShippingDateAndStatusOrderByIdAsc(LocalDate shippingDate, OrderStatus status);
}