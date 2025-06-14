package com.coffeeshop.auth_service.repository;

import com.coffeeshop.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByShopRegistrationNumber(String shopRegistrationNumber);
    User findByMobile(String mobile);
}