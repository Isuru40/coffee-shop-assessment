package com.coffeeshop.order_service.entity;

import java.util.UUID;

public class CancelOrderRequest {
    private UUID orderId;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
}