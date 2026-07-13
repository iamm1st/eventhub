package com.eventhub.security;

import com.eventhub.entity.User;
import com.eventhub.enums.RoleName;
import com.eventhub.enums.UserStatus;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomUserDetailsTest {

    @Test
    void shouldReturnUserDataForActiveUser() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER, RoleName.ROLE_ORGANIZER);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        assertEquals(user.getId(), userDetails.getId());
        assertEquals(user, userDetails.getUser());
        assertEquals(user.getEmail(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertTrue(userDetails.isEnabled());

        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ORGANIZER")));
    }

    @Test
    void shouldDisableBlockedUser() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        user.setStatus(UserStatus.BLOCKED);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        assertFalse(userDetails.isEnabled());
    }
}