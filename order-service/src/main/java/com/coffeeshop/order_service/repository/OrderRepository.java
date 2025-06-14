package com.coffeeshop.order_service.repository;

import com.coffeeshop.order_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByOrderId(Long orderId);
}