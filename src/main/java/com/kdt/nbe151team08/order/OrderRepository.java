package com.kdt.nbe151team08.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByEmailOrderByCreatedAtDesc(String email);
}
