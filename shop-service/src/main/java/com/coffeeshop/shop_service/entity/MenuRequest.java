package com.coffeeshop.shop_service.entity;

import java.util.List;

public class MenuRequest {
    private Long shopId;
    private List<MenuItem> menuItems;

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }
}