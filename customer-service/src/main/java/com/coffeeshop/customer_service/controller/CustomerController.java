package com.coffeeshop.customer_service.controller;

import com.coffeeshop.customer_service.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/{customerId}/shops")
    public ResponseEntity<?> getNearbyShops(@PathVariable Long customerId, @RequestParam Double latitude, @RequestParam Double longitude) {
        return customerService.getNearbyShops(customerId, latitude, longitude);
    }
}