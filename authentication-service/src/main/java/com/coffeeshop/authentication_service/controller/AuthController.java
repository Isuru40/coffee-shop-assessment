package com.coffeeshop.auth_service.controller;

import com.coffeeshop.auth_service.entity.User;
import com.coffeeshop.auth_service.entity.LoginRequest;
import com.coffeeshop.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
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

    @PostMapping("/token")
    @Operation(summary = "Issue client token", description = "Issues a JWT token for client credentials")
    @ApiResponse(responseCode = "200", description = "Token issued successfully")
    @ApiResponse(responseCode = "400", description = "Invalid client credentials or scope")
    public ResponseEntity<Map<String, Object>> issueClientToken(@RequestBody MultiValueMap<String, String> formData) {
        String clientId = formData.getFirst("client_id");
        String clientSecret = formData.getFirst("client_secret");
        String scope = formData.getFirst("scope");
        String grantType = formData.getFirst("grant_type");

        if (!"client_credentials".equals(grantType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "unsupported_grant_type"));
        }

        try {
            Map<String, Object> tokenResponse = authService.issueClientToken(clientId, clientSecret, scope);
            return ResponseEntity.ok(tokenResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Validate user by ID", description = "Checks if a user exists and is a customer")
    @ApiResponse(responseCode = "200", description = "User is a valid customer")
    @ApiResponse(responseCode = "404", description = "User not found or not a customer")
    public ResponseEntity<Void> validateUserById(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        String result = authService.validateToken(token.replace("Bearer ", ""));
        if (result != null) {
            return ResponseEntity.status(401).body(null);
        }
        if (authService.validateUserById(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/test/auth")
    @Operation(summary = "Test authentication", description = "Test endpoint to check authentication service")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.ok("auth ok");
    }
}