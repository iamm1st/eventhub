package com.eventhub.service.impl;

import com.eventhub.dto.request.LoginRequest;
import com.eventhub.dto.request.RegisterRequest;
import com.eventhub.dto.response.AuthResponse;
import com.eventhub.entity.Role;
import com.eventhub.entity.User;
import com.eventhub.enums.RoleName;
import com.eventhub.enums.UserStatus;
import com.eventhub.exception.EmailAlreadyExistsException;
import com.eventhub.exception.InvalidCredentialsException;
import com.eventhub.exception.RoleNotFoundException;
import com.eventhub.exception.UserBlockedException;
import com.eventhub.exception.UsernameAlreadyExistsException;
import com.eventhub.mapper.UserMapper;
import com.eventhub.repository.RoleRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CustomUserDetails;
import com.eventhub.security.JwtService;
import com.eventhub.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(request.getUsername());
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RoleNotFoundException(RoleName.ROLE_USER));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(new CustomUserDetails(savedUser));

        return AuthResponse.builder()
                .accessToken(token)
                .user(userMapper.toResponse(savedUser))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new UserBlockedException(request.getEmail());
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException | DisabledException exception) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(new CustomUserDetails(user));

        return AuthResponse.builder()
                .accessToken(token)
                .user(userMapper.toResponse(user))
                .build();
    }
}