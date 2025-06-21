package com.coffeeshop.order_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Value("${auth-service.token-url}")
    private String tokenUrl;

    @Value("${auth-service.client-id}")
    private String clientId;

    @Value("${auth-service.client-secret}")
    private String clientSecret;

    @Value("${auth-service.scope}")
    private String scope;

    private final RestTemplate restTemplate;
    private final ConcurrentHashMap<String, Token> tokenCache = new ConcurrentHashMap<>();

    public TokenService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAccessToken() {
        Token cachedToken = tokenCache.get("access_token");
        if (cachedToken != null && !cachedToken.isExpired()) {
            logger.debug("Using cached access token");
            return cachedToken.getAccessToken();
        }

        logger.debug("Fetching new access token from {}", tokenUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("scope", scope);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                Integer expiresIn = ((Number) response.getBody().get("expires_in")).intValue();
                if (accessToken != null && expiresIn != null) {
                    Token token = new Token(accessToken, expiresIn);
                    tokenCache.put("access_token", token);
                    logger.info("New access token fetched successfully");
                    return accessToken;
                }
            }
            logger.error("Failed to fetch access token: Invalid response");
        } catch (Exception e) {
            logger.error("Error fetching access token: {}", e.getMessage());
        }
        return null;
    }

    private static class Token {
        private final String accessToken;
        private final long expiryTime;

        public Token(String accessToken, int expiresIn) {
            this.accessToken = accessToken;
            this.expiryTime = System.currentTimeMillis() + (expiresIn * 1000L) - 30000; // 30s buffer
        }

        public String getAccessToken() {
            return accessToken;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() >= expiryTime;
        }
    }
}