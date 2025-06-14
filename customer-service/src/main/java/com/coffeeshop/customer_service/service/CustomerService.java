package com.coffeeshop.customer_service.service;

import com.coffeeshop.customer_service.entity.Customer;
import com.coffeeshop.customer_service.repository.CustomerRepository;
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
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${auth-service.url:http://auth-service:8085/api/v1/auth/validate}")
    private String authServiceUrl;

    @Transactional
    public ResponseEntity<?> getNearbyShops(Long customerId, Double latitude, Double longitude) {
        logger.info("Fetching nearby shops for customer {} at lat: {}, long: {}", customerId, latitude, longitude);
        return ResponseEntity.ok("Nearby shops for customer " + customerId);
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