package com.coffeeshop.shop_service.controller;

import com.coffeeshop.shop_service.entity.Shop;
import com.coffeeshop.shop_service.service.ShopService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/shops")
public class ShopController {

    private static final Logger logger = LoggerFactory.getLogger(ShopController.class);

    @Autowired
    private ShopService shopService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/{shopId}/menu")
    public ResponseEntity<?> configureMenu(@PathVariable Long shopId, @Valid @RequestBody MenuRequest menuRequest) {
        try {
            String menuJson = objectMapper.writeValueAsString(menuRequest.getItems());
            String result = shopService.configureMenu(shopId, menuJson);
            logger.info("Menu configured successfully for shop {}: {}", shopId, result);
            return ResponseEntity.ok(new SuccessResponse(result));
        } catch (Exception e) {
            logger.error("Error configuring menu for shop {}: {}", shopId, e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{shopId}/menu")
    public ResponseEntity<?> updateMenu(@PathVariable Long shopId, @Valid @RequestBody MenuRequest menuRequest) {
        try {
            String menuJson = objectMapper.writeValueAsString(menuRequest.getItems());
            String result = shopService.updateMenu(shopId, menuJson);
            logger.info("Menu updated successfully for shop {}: {}", shopId, result);
            return ResponseEntity.ok(new SuccessResponse(result));
        } catch (Exception e) {
            logger.error("Error updating menu for shop {}: {}", shopId, e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<?> getMenu(@PathVariable Long shopId) {
        try {
            Shop shop = shopService.getMenu(shopId);
            logger.info("Menu fetched successfully for shop {}", shopId);
            return ResponseEntity.ok(shop);
        } catch (Exception e) {
            logger.error("Error fetching menu for shop {}: {}", shopId, e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

//    @GetMapping("/test/hello")
//    public ResponseEntity<String> helloWorld() {
//        logger.info("Hello World endpoint accessed");
//        return ResponseEntity.ok("Hello World");
//    }
//
//    @GetMapping("/test/connectivity")
//    public ResponseEntity<String> testConnectivity() {
//        boolean connected = shopService.testAuthConnectivity();
//        logger.info("Auth connectivity test result: {}", connected ? "Success" : "Failed");
//        return ResponseEntity.ok("Auth connectivity: " + (connected ? "Success" : "Failed"));
//    }

    @GetMapping("/nearby")
    public ResponseEntity<?> findNearbyShops(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double minDistance,
            @RequestParam double maxDistance) {
        try {
            List<Shop> shops = shopService.findNearbyShops(latitude, longitude, minDistance, maxDistance);
            logger.info("Found {} shops near ({}, {}) within {} to {} km", shops.size(), latitude, longitude, minDistance, maxDistance);
            return ResponseEntity.ok(shops);
        } catch (Exception e) {
            logger.error("Error finding nearby shops: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}

class MenuRequest {
    @Valid
    private List<Map<String, Object>> items;

    public List<Map<String, Object>> getItems() {
        return items;
    }

    public void setItems(List<Map<String, Object>> items) {
        this.items = items;
    }
}

class SuccessResponse {
    private String message;

    public SuccessResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
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