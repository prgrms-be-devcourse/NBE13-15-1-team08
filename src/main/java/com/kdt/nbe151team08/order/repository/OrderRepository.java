package com.kdt.nbe151team08.order.repository;


import com.kdt.nbe151team08.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query(value = "SELECT * FROM orders WHERE email = BINARY :email ORDER BY created_at DESC, id DESC",
            nativeQuery = true)
    List<Order> findByEmailOrderByCreatedAtDesc(@Param("email") String email);
}
