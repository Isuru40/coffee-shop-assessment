package com.coffeeshop.auth_service.config;

import com.coffeeshop.auth_service.entity.User;
import com.coffeeshop.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_ExistingUser_ShouldReturnUserDetails() {
        User user = new User();
        user.setUsername("owner1");
        user.setPassword("encodedPass");
        user.setUserType("shop_owner");

        when(userRepository.findByUsername("owner1")).thenReturn(user);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("owner1");

        assertEquals("owner1", userDetails.getUsername());
        assertEquals("encodedPass", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SHOP_OWNER")));
    }

    @Test
    void loadUserByUsername_NonExistingUser_ShouldThrowException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistent");
        });

        assertEquals("User not found with username: nonexistent", exception.getMessage());
    }
}