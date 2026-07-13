package com.eventhub.security;

import com.eventhub.entity.User;
import com.eventhub.enums.RoleName;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentUserProviderTest {

    private final CurrentUserProvider currentUserProvider = new CurrentUserProvider();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserDetailsShouldReturnAuthenticatedUserDetails() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails actualUserDetails = currentUserProvider.getCurrentUserDetails();

        assertEquals(userDetails, actualUserDetails);
    }

    @Test
    void getCurrentUserIdShouldReturnAuthenticatedUserId() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Long currentUserId = currentUserProvider.getCurrentUserId();

        assertEquals(user.getId(), currentUserId);
    }

    @Test
    void getCurrentUserDetailsShouldThrowExceptionWhenAuthenticationIsMissing() {
        SecurityContextHolder.clearContext();

        assertThrows(AuthenticationCredentialsNotFoundException.class, currentUserProvider::getCurrentUserDetails);
    }

    @Test
    void getCurrentUserDetailsShouldThrowExceptionWhenPrincipalIsNotCustomUserDetails() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("anonymousUser", null);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(AuthenticationCredentialsNotFoundException.class, currentUserProvider::getCurrentUserDetails);
    }
}