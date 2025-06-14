package com.coffeeshop.customer_service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "customers", schema = "customer_schema")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "order_history", columnDefinition = "json")
    private String orderHistory;

    @Column(name = "queue_position")
    private Integer queuePosition;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getOrderHistory() { return orderHistory; }
    public void setOrderHistory(String orderHistory) { this.orderHistory = orderHistory; }
    public Integer getQueuePosition() { return queuePosition; }
    public void setQueuePosition(Integer queuePosition) { this.queuePosition = queuePosition; }
}