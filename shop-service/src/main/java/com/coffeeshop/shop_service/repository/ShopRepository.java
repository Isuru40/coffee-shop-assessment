package com.coffeeshop.shop_service.repository;

import com.coffeeshop.shop_service.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Long> {
}