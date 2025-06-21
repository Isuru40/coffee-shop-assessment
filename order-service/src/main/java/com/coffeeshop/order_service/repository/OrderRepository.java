package com.coffeeshop.order_service.repository;

import com.coffeeshop.order_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByOrderId(UUID orderId);
    Optional<Order> findByCustomerIdAndMenuItem(Long customerId, String menuItem);
    Optional<Order> findByCustomerIdAndMenuItemAndStatus(Long customerId, String menuItem, String status);
}