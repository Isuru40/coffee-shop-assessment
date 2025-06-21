package com.coffeeshop.auth_service.repository;

import com.coffeeshop.auth_service.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findByClientId(String clientId);
}