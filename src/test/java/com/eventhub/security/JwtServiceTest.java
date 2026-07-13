package com.eventhub.security;

import com.eventhub.config.properties.JwtProperties;
import com.eventhub.entity.User;
import com.eventhub.enums.RoleName;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "3f7c1a9d2e5b4f8a6c0d9e1f3a5b7c9d4e6f8a0b2c4d6e8f1a3b5c7d9e0f2a4b";

    private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, 86_400_000L));

    @Test
    void generateTokenShouldCreateValidTokenForUser() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        String token = jwtService.generateToken(userDetails);

        assertEquals(user.getEmail(), jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValidShouldReturnFalseForAnotherUser() {
        User firstUser = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User secondUser = TestDataFactory.user(2L, RoleName.ROLE_USER);

        CustomUserDetails firstUserDetails = new CustomUserDetails(firstUser);
        CustomUserDetails secondUserDetails = new CustomUserDetails(secondUser);

        String token = jwtService.generateToken(firstUserDetails);

        assertFalse(jwtService.isTokenValid(token, secondUserDetails));
    }
}