package com.coffeeshop.auth_service.service;

import com.coffeeshop.auth_service.config.JwtUtil;
import com.coffeeshop.auth_service.entity.User;
import com.coffeeshop.auth_service.exception.UserAlreadyExistsException;
import com.coffeeshop.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_ValidShopOwner_ShouldRegisterSuccessfully() {
        User user = new User();
        user.setUsername("owner1");
        user.setPassword("Pass1234");
        user.setUserType("shop_owner");
        user.setShopRegistrationNumber("SHOP123");
        user.setMobile("1234567890");

        when(userRepository.findByUsername("owner1")).thenReturn(null);
        when(userRepository.findByShopRegistrationNumber("SHOP123")).thenReturn(null);
        when(passwordEncoder.encode("Pass1234")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(user);

        String result = authService.registerUser(user);

        assertEquals("Registration successful for shop_owner with username: owner1", result);
        verify(userRepository).save(any(User.class));
        assertEquals("encodedPass", user.getPassword());
    }

    @Test
    void registerUser_DuplicateShopOwner_ShouldThrowException() {
        User user = new User();
        user.setUsername("owner1");
        user.setPassword("Pass1234");
        user.setUserType("shop_owner");
        user.setShopRegistrationNumber("SHOP123");

        when(userRepository.findByUsername("owner1")).thenReturn(null);
        when(userRepository.findByShopRegistrationNumber("SHOP123")).thenReturn(new User());

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.registerUser(user);
        });

        assertEquals("User already exists with registration number: SHOP123", exception.getMessage());
    }

    @Test
    void registerUser_DuplicateUsername_ShouldThrowException() {
        User user = new User();
        user.setUsername("owner1");
        user.setPassword("Pass1234");
        user.setUserType("shop_owner");
        user.setShopRegistrationNumber("SHOP123");

        when(userRepository.findByUsername("owner1")).thenReturn(new User());

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.registerUser(user);
        });

        assertEquals("User already exists with username: owner1", exception.getMessage());
    }

    @Test
    void loginUser_ValidCredentials_ShouldReturnToken() {
        User user = new User();
        user.setUsername("owner1");
        user.setPassword("encodedPass");

        when(userRepository.findByUsername("owner1")).thenReturn(user);
        when(passwordEncoder.matches("Pass1234", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken("owner1")).thenReturn("jwt-token");

        Map<String, String> response = authService.loginUser("owner1", "Pass1234");

        assertEquals("jwt-token", response.get("token"));
        assertEquals("Login successful", response.get("message"));
    }

    @Test
    void loginUser_InvalidCredentials_ShouldThrowException() {
        when(userRepository.findByUsername("owner1")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.loginUser("owner1", "Pass1234");
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void validateToken_ValidToken_ShouldReturnNull() {
        when(jwtUtil.validateToken("jwt-token")).thenReturn(true);

        String result = authService.validateToken("jwt-token");

        assertNull(result);
    }

    @Test
    void validateToken_InvalidToken_ShouldReturnErrorMessage() {
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        String result = authService.validateToken("invalid-token");

        assertEquals("Invalid token", result);
    }
}