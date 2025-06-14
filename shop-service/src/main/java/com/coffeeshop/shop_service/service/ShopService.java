package com.coffeeshop.shop_service.service;

import com.coffeeshop.shop_service.entity.Shop;
import com.coffeeshop.shop_service.repository.ShopRepository;
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

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// [Keep existing @Service annotation]
@Service
public class ShopService {

    private static final Logger logger = LoggerFactory.getLogger(ShopService.class);

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${auth-service.url}")
    private String authServiceUrl;

    private static final double EARTH_RADIUS_KM = 6371.0; // Earth's radius in kilometers

    @Transactional
    public String configureMenu(Long shopId, String menu) {
        Shop shop = shopRepository.findById(shopId).orElse(null);
        if (shop == null) {
            shop = new Shop();
            shop.setQueueCount(0);
            shop.setMaxQueueSize(10);
            try {
                java.util.Date openingDate = new SimpleDateFormat("HH:mm:ss").parse("08:00:00");
                java.util.Date closingDate = new SimpleDateFormat("HH:mm:ss").parse("18:00:00");
                shop.setOpeningTime(new java.sql.Time(openingDate.getTime()));
                shop.setClosingTime(new java.sql.Time(closingDate.getTime()));
            } catch (ParseException e) {
                logger.error("Failed to parse default times: {}", e.getMessage());
            }
        }
        shop.setMenu(menu);
        shop = shopRepository.save(shop);
        return "Menu updated for shop " + shop.getId();
    }

    public Shop getMenu(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found: " + shopId));
    }

    @Transactional
    public String updateMenu(Long shopId, String menu) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found: " + shopId));
        try {
            shop.setMenu(menu);
            shopRepository.save(shop);
            return "Menu updated for shop " + shopId;
        } catch (Exception e) {
            logger.error("Error saving menu: {}", e.getMessage());
            throw new RuntimeException("Invalid menu format: " + e.getMessage());
        }
    }

//    public boolean testAuthConnectivity() {
//        String testUrl = "http://authentication-service:8085/api/v1/auth/test/auth";
//        HttpHeaders headers = new HttpHeaders();
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        try {
//            ResponseEntity<String> response = restTemplate.exchange(
//                    testUrl,
//                    HttpMethod.GET,
//                    entity,
//                    String.class
//            );
//            logger.debug("Auth connectivity test: Status={}, Body={}", response.getStatusCode(), response.getBody());
//            return response.getStatusCode().is2xxSuccessful() && "auth ok".equals(response.getBody());
//        } catch (Exception e) {
//            logger.error("Auth connectivity test failed: {}", e.getMessage());
//            return false;
//        }
//    }

    public boolean validateToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        logger.debug("Attempting to validate token with URL: {}", authServiceUrl);
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

    public double calculateDistance(Long shopId, double userLat, double userLon) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found: " + shopId));
        if (shop.getLatitude() == null || shop.getLongitude() == null) {
            throw new RuntimeException("Shop location coordinates are not available");
        }

        // Convert BigDecimal to double
        double shopLat = shop.getLatitude().doubleValue();
        double shopLon = shop.getLongitude().doubleValue();

        // Convert latitude and longitude to radians
        double lat1 = Math.toRadians(userLat);
        double lon1 = Math.toRadians(userLon);
        double lat2 = Math.toRadians(shopLat);
        double lon2 = Math.toRadians(shopLon);

        // Haversine formula
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        logger.debug("Calculated distance to shop {}: {} km", shopId, distance);
        return distance; // Distance in kilometers
    }

    public List<Shop> findNearbyShops(double userLat, double userLon, double minDistance, double maxDistance) {
        List<Shop> allShops = shopRepository.findAll();
        return allShops.stream()
                .filter(shop -> {
                    if (shop.getLatitude() == null || shop.getLongitude() == null) {
                        return false;
                    }
                    double distance = calculateDistance(shop.getId(), userLat, userLon);
                    return distance >= minDistance && distance <= maxDistance;
                })
                .collect(Collectors.toList());
    }
}