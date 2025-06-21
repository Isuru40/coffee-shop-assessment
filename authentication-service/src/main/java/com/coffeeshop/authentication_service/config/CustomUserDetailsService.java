package com.coffeeshop.auth_service.config;

import com.coffeeshop.auth_service.entity.Client;
import com.coffeeshop.auth_service.entity.User;
import com.coffeeshop.auth_service.repository.ClientRepository;
import com.coffeeshop.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try to find a user first
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    Collections.emptyList() // Adjust roles if needed
            );
        }

        // Try to find a client if no user is found
        Client client = clientRepository.findByClientId(username);
        if (client != null) {
            return new org.springframework.security.core.userdetails.User(
                    client.getClientId(),
                    client.getClientSecret(),
                    Collections.emptyList() // Adjust roles if needed
            );
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}