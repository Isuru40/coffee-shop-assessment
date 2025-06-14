package com.coffeeshop.order_service.entity;

public class OrderRequest {
    private Long customerId;
    private Long shopId;
    private String menuItem;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getShopId() { return shopId; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public String getMenuItem() { return menuItem; }
    public void setMenuItem(String menuItem) { this.menuItem = menuItem; }
}