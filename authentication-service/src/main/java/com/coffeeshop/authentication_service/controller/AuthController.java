package com.coffeeshop.auth_service.controller;

import com.coffeeshop.auth_service.entity.User;
import com.coffeeshop.auth_service.entity.LoginRequest;
import com.coffeeshop.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a user with the specified details")
    @ApiResponse(responseCode = "200", description = "Registration successful")
    @ApiResponse(responseCode = "400", description = "Invalid user data")
    public ResponseEntity<String> register(@RequestBody User user) {
        String message = authService.registerUser(user);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        Map<String, String> response = authService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token", description = "Validates the provided JWT token")
    @ApiResponse(responseCode = "200", description = "Token is valid")
    @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String token) {
        String result = authService.validateToken(token.replace("Bearer ", ""));
        if (result == null) {
            return ResponseEntity.ok("Token is valid");
        }
        return ResponseEntity.status(401).body(result);
    }

    @GetMapping("/test/auth")
    @Operation(summary = "Test authentication", description = "Test endpoint to check authentication service")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.ok("auth ok");
    }
}