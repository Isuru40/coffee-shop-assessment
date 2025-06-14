package com.coffeeshop.customer_service.repository;

import com.coffeeshop.customer_service.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByCustomerId(Long customerId);
}