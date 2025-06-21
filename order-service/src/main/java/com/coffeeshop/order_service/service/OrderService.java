package com.coffeeshop.order_service.service;

import com.coffeeshop.order_service.entity.Order;
import com.coffeeshop.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TokenService tokenService;

    @Value("${auth-service.url}")
    private String authServiceUrl;

    @Value("${shop-service.url}")
    private String shopServiceUrl;

    @Transactional
    public ResponseEntity<?> processOrder(Long customerId, Long shopId, String menuItem, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            logger.warn("Invalid quantity: {}", quantity);
            return ResponseEntity.badRequest().body("Quantity must be a positive integer");
        }

//        // Validate customerId
//        if (!validateCustomerId(customerId)) {
//            logger.warn("Invalid customer ID: {}", customerId);
//            return ResponseEntity.badRequest().body("Invalid customer ID: " + customerId);
//        }

        // Validate shopId and menuItem
        if (!validateMenuItem(shopId, menuItem)) {
            logger.warn("Invalid shop ID {} or menu item: {}", shopId, menuItem);
            return ResponseEntity.badRequest().body("Invalid shop ID or menu item: " + menuItem);
        }

        // Check for existing order with status PENDING
        Optional<Order> existingOrder = orderRepository.findByCustomerIdAndMenuItemAndStatus(customerId, menuItem, "PENDING");
        if (existingOrder.isPresent()) {
            UUID existingOrderId = existingOrder.get().getOrderId();
            logger.info("Order already exists for customer {} and menu item {}: {}", customerId, menuItem, existingOrderId);
            return ResponseEntity.status(200).body("Order " + existingOrderId + " already exists for customer " + customerId + " and menu item " + menuItem);
        }

        // Create new order
        UUID orderId = UUID.randomUUID();
        logger.info("Processing new order {} for customer {} at shop {} with quantity {}", orderId, customerId, shopId, quantity);
        Order order = new Order();
        order.setOrderId(orderId);
        order.setCustomerId(customerId);
        order.setShopId(shopId);
        order.setMenuItem(menuItem);
        order.setQuantity(quantity);
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
        orderRepository.save(order);
        return ResponseEntity.status(201).body("Order " + orderId + " created");
    }

    @Transactional
    public ResponseEntity<?> getOrderStatus(UUID orderId) {
        logger.info("Retrieving status for order {}", orderId);
        Order order = orderRepository.findByOrderId(orderId);
        if (order != null) {
            return ResponseEntity.ok("Status of order " + orderId + ": " + order.getStatus());
        }
        return ResponseEntity.notFound().build();
    }

    @Transactional
    public ResponseEntity<?> cancelOrder(UUID orderId) {
        logger.info("Cancelling order {}", orderId);
        Order order = orderRepository.findByOrderId(orderId);
        if (order != null) {
            order.setStatus("CANCELLED");
            orderRepository.save(order);
            return ResponseEntity.ok("Order " + orderId + " cancelled");
        }
        return ResponseEntity.notFound().build();
    }

    private boolean validateCustomerId(Long customerId) {
        String url = authServiceUrl.replace("/validate", "/users/" + customerId);
        String accessToken = tokenService.getAccessToken();
        if (accessToken == null) {
            logger.error("Failed to obtain access token for customer validation");
            return false;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        logger.debug("Validating customer ID {} with URL: {}", customerId, url);
        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.GET, entity, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.error("Customer validation error for ID {}: {}", customerId, e.getMessage());
            return false;
        }
    }

    private boolean validateMenuItem(Long shopId, String menuItem) {
        String url = shopServiceUrl + "/" + shopId;
        String accessToken = tokenService.getAccessToken();
        if (accessToken == null) {
            logger.error("Failed to obtain access token for shop/menu validation");
            return false;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        logger.debug("Validating shop ID {} and menu item {} with URL: {}", shopId, menuItem, url);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return false;
            }
            logger.debug("Shop response for ID {}: {}", shopId, response.getBody());
            Map<String, Object> shop = response.getBody();
            Map<String, Object> menu = (Map<String, Object>) shop.get("menu");
            if (menu == null || menu.get("menuItems") == null) {
                return false;
            }
            List<Map<String, Object>> menuItems = (List<Map<String, Object>>) menu.get("menuItems");
            return menuItems.stream()
                    .anyMatch(item -> menuItem.equals(item.get("name")));
        } catch (Exception e) {
            logger.error("Shop/menu validation error for shop ID {} and menu item {}: {}", shopId, menuItem, e.getMessage());
            return false;
        }
    }

    public boolean validateToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        logger.debug("Validating token with URL: {}", authServiceUrl);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    authServiceUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            logger.debug("Validation response: Status={}, Body={}", response.getStatusCode(), response.getBody());
            return "Token is valid".equals(response.getBody());
        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }
}