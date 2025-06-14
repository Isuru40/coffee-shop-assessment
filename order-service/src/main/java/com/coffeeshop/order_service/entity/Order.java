package com.coffeeshop.order_service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "orders", schema = "order_schema")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "menu_item")
    private String menuItem;

    @Column(name = "status")
    private String status;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public String getMenuItem() { return menuItem; }
    public void setMenuItem(String menuItem) { this.menuItem = menuItem; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}