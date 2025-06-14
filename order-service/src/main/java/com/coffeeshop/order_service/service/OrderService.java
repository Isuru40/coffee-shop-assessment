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

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${auth-service.url}")
    private String authServiceUrl;

    @Transactional
    public ResponseEntity<?> processOrder(Long orderId, Long customerId, Long shopId, String menuItem) {
        logger.info("Processing order {} for customer {} at shop {}", orderId, customerId, shopId);
        Order order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            order = new Order();
            order.setOrderId(orderId);
            order.setCustomerId(customerId);
            order.setShopId(shopId);
            order.setMenuItem(menuItem);
            order.setStatus("PENDING");
            orderRepository.save(order);
            return ResponseEntity.status(201).body("Order " + orderId + " created");
        }
        return ResponseEntity.status(200).body("Order " + orderId + " already exists");
    }

    @Transactional
    public ResponseEntity<?> getOrderStatus(Long orderId) {
        logger.info("Retrieving status for order {}", orderId);
        Order order = orderRepository.findByOrderId(orderId);
        if (order != null) {
            return ResponseEntity.ok("Status of order " + orderId + ": " + order.getStatus());
        }
        return ResponseEntity.notFound().build();
    }

    @Transactional
    public ResponseEntity<?> cancelOrder(Long orderId) {
        logger.info("Cancelling order {}", orderId);
        Order order = orderRepository.findByOrderId(orderId);
        if (order != null) {
            order.setStatus("CANCELLED");
            orderRepository.save(order);
            return ResponseEntity.ok("Order " + orderId + " cancelled");
        }
        return ResponseEntity.notFound().build();
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