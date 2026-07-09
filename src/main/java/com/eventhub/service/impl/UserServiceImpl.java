package com.eventhub.service.impl;

import com.eventhub.aspect.LogAction;
import com.eventhub.dto.request.UserUpdateRequest;
import com.eventhub.dto.response.UserResponse;
import com.eventhub.entity.User;
import com.eventhub.enums.UserStatus;
import com.eventhub.exception.auth.EmailAlreadyExistsException;
import com.eventhub.exception.auth.UsernameAlreadyExistsException;
import com.eventhub.exception.user.AdminSelfBlockException;
import com.eventhub.exception.user.UserAlreadyActiveException;
import com.eventhub.exception.user.UserAlreadyBlockedException;
import com.eventhub.exception.user.UserNotFoundException;
import com.eventhub.mapper.UserMapper;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        User user = findUserById(currentUserId);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUser(UserUpdateRequest request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        User user = findUserById(currentUserId);

        String normalizedUsername = normalizeText(request.getUsername());
        String normalizedEmail = normalizeText(request.getEmail());

        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(normalizedUsername, user.getId())) {
            throw new UsernameAlreadyExistsException(normalizedUsername);
        }

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, user.getId())) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserById(id);

        return userMapper.toResponse(user);
    }

    @Override
    @LogAction(action = "BLOCK_USER", entityType = "USER", entityIdArgIndex = 0)
    @Transactional
    public UserResponse blockUser(Long id) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        if (currentUserId.equals(id)) {
            throw new AdminSelfBlockException();
        }

        User user = findUserById(id);

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new UserAlreadyBlockedException(id);
        }

        user.setStatus(UserStatus.BLOCKED);

        return userMapper.toResponse(user);
    }

    @Override
    @LogAction(action = "UNBLOCK_USER", entityType = "USER", entityIdArgIndex = 0)
    @Transactional
    public UserResponse unblockUser(Long id) {
        User user = findUserById(id);

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new UserAlreadyActiveException(id);
        }

        user.setStatus(UserStatus.ACTIVE);

        return userMapper.toResponse(user);
    }

    private User findUserById(Long id) {
        return userRepository.findByIdWithRoles(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    private String normalizeText(String value) {
        return value.trim();
    }
}