package com.kdt.nbe151team08.order.repository;


import com.kdt.nbe151team08.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByEmailOrderByCreatedAtDesc(String email);
}
