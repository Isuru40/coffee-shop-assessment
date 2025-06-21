package com.coffeeshop.auth_service.service;

import com.coffeeshop.auth_service.config.JwtUtil;
import com.coffeeshop.auth_service.entity.Client;
import com.coffeeshop.auth_service.entity.User;
import com.coffeeshop.auth_service.exception.UserAlreadyExistsException;
import com.coffeeshop.auth_service.repository.ClientRepository;
import com.coffeeshop.auth_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    public String registerUser(User user) {
        if (user.getPassword() == null || !PASSWORD_PATTERN.matcher(user.getPassword()).matches()) {
            throw new IllegalArgumentException("Password must be at least 8 characters long, with at least one uppercase letter, one lowercase letter, and one digit");
        }

        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new UserAlreadyExistsException("User already exists with username: " + user.getUsername());
        }

        if ("shop_owner".equalsIgnoreCase(user.getUserType())) {
            if (userRepository.findByShopRegistrationNumber(user.getShopRegistrationNumber()) != null) {
                throw new UserAlreadyExistsException("User already exists with registration number: " + user.getShopRegistrationNumber());
            }
        } else {
            if (userRepository.findByMobile(user.getMobile()) != null) {
                throw new UserAlreadyExistsException("User already exists with mobile number: " + user.getMobile());
            }
        }

        if (user.getUserType() == null || !("shop_owner".equalsIgnoreCase(user.getUserType()) ||
                "shop_operator".equalsIgnoreCase(user.getUserType()) || "customer".equalsIgnoreCase(user.getUserType()))) {
            throw new IllegalArgumentException("Invalid userType. Must be shop_owner, shop_operator, or customer");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        logger.info("User registered successfully: {}", user.getUsername());
        return "Registration successful for " + user.getUserType() + " with username: " + user.getUsername();
    }

    public Map<String, String> loginUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            String token = jwtUtil.generateToken(username);
            logger.info("Generated token for user: {}", username);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Login successful");
            return response;
        }
        logger.warn("Login failed for username: {}", username);
        throw new RuntimeException("Invalid username or password");
    }

    public String validateToken(String token) {
        try {
            if (jwtUtil.validateToken(token)) {
                logger.info("Token validated successfully: {}", token.substring(0, Math.min(token.length(), 20)));
                return null;
            }
            logger.warn("Invalid token detected");
            return "Invalid token";
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return "Token expired or invalid: " + e.getMessage();
        }
    }

    public Map<String, Object> issueClientToken(String clientId, String clientSecret, String scope) {
        logger.info("Issuing client token for clientId: {}", clientId);
        Client client = clientRepository.findByClientId(clientId);
        if (client == null || !passwordEncoder.matches(clientSecret, client.getClientSecret())) {
            logger.warn("Invalid client credentials for clientId: {}", clientId);
            throw new RuntimeException("Invalid client credentials");
        }
        logger.info("client credentials validated for clientId: {}", clientId);
        String[] requestedScopes = scope != null ? scope.split(" ") : new String[]{};
        String[] allowedScopes = client.getScopes() != null ? client.getScopes().split(" ") : new String[]{};
        boolean validScope = Arrays.stream(requestedScopes).allMatch(s -> Arrays.asList(allowedScopes).contains(s));
        if (!validScope) {
            logger.warn("Invalid scope requested: {} for clientId: {}", scope, clientId);
            throw new RuntimeException("Invalid scope");
        }

        String token = jwtUtil.generateToken(clientId);
        logger.info("Generated client token for clientId: {}", clientId);
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", token);
        response.put("token_type", "Bearer");
        response.put("expires_in", jwtUtil.getExpiration() / 1000);
        response.put("scope", scope);
        return response;
    }

    public boolean validateUserById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            logger.warn("User not found with ID: {}", userId);
            return false;
        }
        if (!"customer".equalsIgnoreCase(user.getUserType())) {
            logger.warn("User ID {} is not a customer: {}", userId, user.getUserType());
            return false;
        }
        logger.info("Validated user ID {} as customer", userId);
        return true;
    }
}