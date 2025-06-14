package com.coffeeshop.shop_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.coffeeshop.shop_service.service.ShopService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// [Keep existing @Component annotation]
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private ShopService shopService;

    @Autowired
    private UserDetailsService userDetailsService; // Add this for authentication context

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String jwt = null;
        logger.debug("Token received: {}", header);

        // Check if the request is for permitted endpoints
        String path = request.getServletPath();
        if (path.equals("/api/v1/shops/test/hello") || path.equals("/api/v1/shops/test/connectivity")) {
            logger.debug("Bypassing authentication for permitted endpoint: {}", path);
            chain.doFilter(request, response);
            return;
        }

        if (header != null && header.startsWith("Bearer ")) {
            jwt = header.substring(7);
            logger.debug("Extracted JWT: {}", jwt);
            if (shopService.validateToken(jwt)) {
                logger.debug("Token validated successfully: {}", jwt);
                // Extract username (simplified, assume it's in the token subject)
                String username = extractUsernameFromToken(jwt); // Placeholder, refine as needed
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.warn("Invalid token: {}, proceeding to Spring Security", jwt);
            }
        } else {
            logger.debug("No Bearer token found, proceeding to Spring Security");
        }

        chain.doFilter(request, response); // Always continue to next filter
    }

    // Placeholder method to extract username from token (refine with JWT parsing if needed)
    private String extractUsernameFromToken(String token) {
        // This is a simplification; in a real scenario, parse the JWT (e.g., using jjwt)
        // For now, assume the username is a fixed part of the token (e.g., substring or dummy)
        return "user"; // Replace with actual JWT parsing logic if required
    }
}