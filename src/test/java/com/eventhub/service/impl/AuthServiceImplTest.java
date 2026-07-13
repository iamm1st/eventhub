package com.eventhub.service.impl;

import com.eventhub.dto.request.LoginRequest;
import com.eventhub.dto.request.RegisterRequest;
import com.eventhub.dto.response.AuthResponse;
import com.eventhub.dto.response.UserResponse;
import com.eventhub.entity.Role;
import com.eventhub.entity.User;
import com.eventhub.enums.RoleName;
import com.eventhub.enums.UserStatus;
import com.eventhub.exception.auth.EmailAlreadyExistsException;
import com.eventhub.exception.auth.InvalidCredentialsException;
import com.eventhub.exception.auth.UserBlockedException;
import com.eventhub.mapper.UserMapper;
import com.eventhub.repository.RoleRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.JwtService;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerShouldCreateUserWithRoleUserAndReturnToken() {
        RegisterRequest request = RegisterRequest.builder().username("polina").email("polina@mail.com").password("123456").build();

        Role userRole = TestDataFactory.role(RoleName.ROLE_USER);

        UserResponse userResponse = UserResponse.builder()
                .id(2L)
                .username(request.getUsername())
                .email(request.getEmail())
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(request.getUsername())).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        AuthResponse response = authService.register(request);

        assertEquals("jwt-token", response.getAccessToken());
        assertEquals(userResponse, response.getUser());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerShouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder().username("polina").email("polina@mail.com").password("123456").build();

        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        LoginRequest request = LoginRequest.builder().email("user1@mail.com").password("123456").build();

        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);

        UserResponse userResponse = UserResponse.builder().id(user.getId()).email(user.getEmail()).build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getAccessToken());
        assertEquals(userResponse, response.getUser());

        verify(authenticationManager).authenticate(any());
    }

    @Test
    void loginShouldThrowExceptionWhenUserIsBlocked() {
        LoginRequest request = LoginRequest.builder().email("user1@mail.com").password("123456").build();

        User blockedUser = TestDataFactory.blockedUser(1L, RoleName.ROLE_USER);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(blockedUser));

        assertThrows(UserBlockedException.class, () -> authService.login(request));

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void loginShouldThrowExceptionWhenPasswordIsWrong() {
        LoginRequest request = LoginRequest.builder().email("user1@mail.com").password("wrong").build();

        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}