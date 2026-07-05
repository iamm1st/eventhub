package com.eventhub.security;

import com.eventhub.entity.User;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
            throw new AuthenticationCredentialsNotFoundException("Authentication is required");
        }

        return customUserDetails;
    }

    public User getCurrentUser() {
        return getCurrentUserDetails().getUser();
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}