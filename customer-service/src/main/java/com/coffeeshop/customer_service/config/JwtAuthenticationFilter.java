package com.coffeeshop.customer_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.coffeeshop.customer_service.service.CustomerService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String jwt = null;
        logger.debug("Token received: {}", header);

        String path = request.getServletPath();
        if (path.equals("/api/v1/customers/test/hello")) {
            logger.debug("Bypassing authentication for permitted endpoint: {}", path);
            chain.doFilter(request, response);
            return;
        }

        if (header != null && header.startsWith("Bearer ")) {
            jwt = header.substring(7);
            logger.debug("Extracted JWT: {}", jwt);
            if (customerService.validateToken(jwt)) {
                logger.debug("Token validated successfully: {}", jwt);
                String username = extractUsernameFromToken(jwt);
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

        chain.doFilter(request, response);
    }

    private String extractUsernameFromToken(String token) {
        return "user"; // Simplified; replace with actual JWT parsing
    }
}