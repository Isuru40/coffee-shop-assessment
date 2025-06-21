package com.coffeeshop.order_service.controller;

import com.coffeeshop.order_service.entity.CancelOrderRequest;
import com.coffeeshop.order_service.entity.OrderRequest;
import com.coffeeshop.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> processOrder(@RequestBody OrderRequest orderRequest) {
        return orderService.processOrder(
                orderRequest.getCustomerId(),
                orderRequest.getShopId(),
                orderRequest.getMenuItem(),
                orderRequest.getQuantity()
        );
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<?> getOrderStatus(@PathVariable UUID orderId) {
        return orderService.getOrderStatus(orderId);
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelOrder(@RequestBody CancelOrderRequest cancelOrderRequest) {
        if (cancelOrderRequest.getOrderId() == null) {
            return ResponseEntity.badRequest().body("Order ID is required");
        }
        return orderService.cancelOrder(cancelOrderRequest.getOrderId());
    }
}