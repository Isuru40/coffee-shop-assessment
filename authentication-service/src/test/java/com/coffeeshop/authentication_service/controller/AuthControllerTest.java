package com.coffeeshop.auth_service.controller;

import com.coffeeshop.auth_service.entity.LoginRequest;
import com.coffeeshop.auth_service.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        String uniqueShopRegNumber = "SHOP-" + UUID.randomUUID().toString().substring(0, 8);
        user = new User();
        user.setUsername("owner1-" + UUID.randomUUID().toString().substring(0, 8)); // Unique username
        user.setPassword("Pass1234");
        user.setUserType("shop_owner");
        user.setMobile("1234567890");
        user.setName("Owner One");
        user.setShopRegistrationNumber(uniqueShopRegNumber);
        user.setLocation("City");
        user.setContactDetails("contact@shop.com");

        loginRequest = new LoginRequest();
        loginRequest.setUsername(user.getUsername());
        loginRequest.setPassword("Pass1234");
    }

    @Test
    void register_ValidUser_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string("Registration successful for shop_owner with username: " + user.getUsername()));
    }

    @Test
    void register_DuplicateUser_ShouldReturnConflict() throws Exception {
        // Register first time
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        // Register again with same details
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict())
                .andExpect(content().string("User already exists with username: " + user.getUsername()));
    }

    @Test
    void login_ValidCredentials_ShouldReturnToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void login_InvalidCredentials_ShouldReturnBadRequest() throws Exception {
        loginRequest.setPassword("WrongPass");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid username or password"));
    }

    @Test
    void validate_ValidToken_ShouldReturnValid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        mockMvc.perform(get("/api/v1/auth/validate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Token is valid"));
    }

//    @Test
//    void validate_InvalidToken_ShouldReturnForbidden() throws Exception {
//        mockMvc.perform(get("/api/v1/auth/validate")
//                        .header("Authorization", "Bearer invalid-token"))
//                .andExpect(status().isForbidden())
//                .andExpect(content().string("Invalid token"));
//    }
}