package com.coffeeshop.shop_service.service;

import com.coffeeshop.shop_service.entity.Menu;
import com.coffeeshop.shop_service.entity.MenuItem;
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

import java.math.BigDecimal;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopService {

    private static final Logger logger = LoggerFactory.getLogger(ShopService.class);
    private static final double EARTH_RADIUS_KM = 6371.0;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${auth-service.url}")
    private String authServiceUrl;

    @Transactional
    public String configureMenu(Long shopId, List<MenuItem> menuItems) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        Menu menu = shop.getMenu();
        if (menu == null) {
            menu = new Menu();
            menu.setShop(shop);
            shop.setMenu(menu);
            // Save the menu first to generate its ID
            menu = shopRepository.save(shop).getMenu();
        }
        // Set and save menuItems with the persisted menu
        menu.setMenuItems(menuItems);
        for (MenuItem item : menuItems) {
            item.setMenu(menu); // Ensure each MenuItem references the persisted Menu
        }
        shopRepository.save(shop);
        return "Menu configured for shop " + shopId;
    }

    @Transactional
    public String updateMenu(Long shopId, List<MenuItem> menuItems) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        Menu menu = shop.getMenu();
        if (menu == null) {
            throw new RuntimeException("Menu not found for shop " + shopId);
        }
        // Clear existing menuItems to handle orphanRemoval correctly
        menu.getMenuItems().clear();
        // Add new menuItems
        for (MenuItem item : menuItems) {
            item.setMenu(menu); // Ensure each MenuItem references the existing Menu
            menu.getMenuItems().add(item);
        }
        shopRepository.save(shop);
        return "Menu updated for shop " + shopId;
    }

    public Shop getShop(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
    }

    @Transactional
    public Shop createShop(String name, BigDecimal latitude, BigDecimal longitude) {
        Shop shop = new Shop();
        shop.setName(name);
        shop.setQueueCount(0);
        shop.setMaxQueueSize(10);
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            shop.setOpeningTime(new Time(timeFormat.parse("08:00:00").getTime()));
            shop.setClosingTime(new Time(timeFormat.parse("18:00:00").getTime()));
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing time", e);
        }
        shop.setLatitude(latitude);
        shop.setLongitude(longitude);
        return shopRepository.save(shop);
    }

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
        return distance;
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