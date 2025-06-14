package com.coffeeshop.order_service.controller;

import com.coffeeshop.order_service.service.OrderService;
import com.coffeeshop.order_service.entity.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/{orderId}")
    public ResponseEntity<?> processOrder(@PathVariable Long orderId, @RequestBody OrderRequest orderRequest) {
        return orderService.processOrder(orderId, orderRequest.getCustomerId(), orderRequest.getShopId(), orderRequest.getMenuItem());
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<?> getOrderStatus(@PathVariable Long orderId) {
        return orderService.getOrderStatus(orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        return orderService.cancelOrder(orderId);
    }
}