package com.eventhub.security;

import com.eventhub.entity.User;
import com.eventhub.enums.RoleName;
import com.eventhub.repository.UserRepository;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsernameShouldReturnCustomUserDetailsWhenUserExists() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Object userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        assertInstanceOf(CustomUserDetails.class, userDetails);
        assertEquals(user.getEmail(), ((CustomUserDetails) userDetails).getUsername());
    }

    @Test
    void loadUserByUsernameShouldThrowExceptionWhenUserNotFound() {
        String email = "missing@mail.ru";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(email));
    }
}