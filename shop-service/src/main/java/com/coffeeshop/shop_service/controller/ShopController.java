package com.coffeeshop.shop_service.controller;

import com.coffeeshop.shop_service.entity.MenuRequest;
import com.coffeeshop.shop_service.entity.Shop;
import com.coffeeshop.shop_service.entity.ShopRequest;
import com.coffeeshop.shop_service.service.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shops")
public class ShopController {

    private static final Logger logger = LoggerFactory.getLogger(ShopController.class);

    @Autowired
    private ShopService shopService;

    @PostMapping("/menus")
    public ResponseEntity<?> configureMenu(@RequestBody MenuRequest menuRequest) {
        try {
            String result = shopService.configureMenu(menuRequest.getShopId(), menuRequest.getMenuItems());
            logger.info("Menu configured successfully for shop {}: {}", menuRequest.getShopId(), result);
            return ResponseEntity.ok(new SuccessResponse(result));
        } catch (Exception e) {
            logger.error("Error configuring menu for shop {}: {}", menuRequest.getShopId(), e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/menus")
    public ResponseEntity<?> updateMenu(@RequestBody MenuRequest menuRequest) {
        try {
            String result = shopService.updateMenu(menuRequest.getShopId(), menuRequest.getMenuItems());
            logger.info("Menu updated successfully for shop {}: {}", menuRequest.getShopId(), result);
            return ResponseEntity.ok(new SuccessResponse(result));
        } catch (Exception e) {
            logger.error("Error updating menu for shop {}: {}", menuRequest.getShopId(), e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<?> getShop(@PathVariable Long shopId) {
        try {
            Shop shop = shopService.getShop(shopId);
            logger.info("Shop fetched successfully for shop {}", shopId);
            return ResponseEntity.ok(shop);
        } catch (Exception e) {
            logger.error("Error fetching shop {}: {}", shopId, e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createShop(@RequestBody ShopRequest shopRequest) {
        try {
            Shop shop = shopService.createShop(shopRequest.getName(), shopRequest.getLatitude(), shopRequest.getLongitude());
            logger.info("Shop created successfully with id: {}", shop.getId());
            return ResponseEntity.ok(shop);
        } catch (Exception e) {
            logger.error("Error creating shop: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> findNearbyShops(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double minDistance,
            @RequestParam double maxDistance) {
        try {
            List<Shop> shops = shopService.findNearbyShops(latitude, longitude, minDistance, maxDistance);
            logger.info("Found {} shops near ({}, {}) within {} to {} km", shops.size(), latitude, longitude, minDistance, maxDistance);
            return ResponseEntity.ok(new SuccessResponse(shops));
        } catch (Exception e) {
            logger.error("Error finding nearby shops: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    class SuccessResponse {
        private Object data;

        public SuccessResponse(Object data) {
            this.data = data;
        }

        public Object getData() {
            return data;
        }
    }

    class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }
    }
}