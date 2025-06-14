package com.coffeeshop.auth_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = AuthenticationServiceApplication.class)
@ActiveProfiles("test") // Use test profile with H2
class AuthenticationServiceApplicationTests {

	@Test
	void contextLoads() {
		// Verifies Spring context loads
	}
}