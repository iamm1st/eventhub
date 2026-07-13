package com.eventhub.service.impl;

import com.eventhub.dto.response.UserResponse;
import com.eventhub.entity.User;
import com.eventhub.enums.RoleName;
import com.eventhub.enums.UserStatus;
import com.eventhub.exception.user.AdminSelfBlockException;
import com.eventhub.exception.user.UserAlreadyBlockedException;
import com.eventhub.mapper.UserMapper;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void blockUserShouldChangeStatusToBlocked() {
        User admin = TestDataFactory.user(1L, RoleName.ROLE_ADMIN);
        User user = TestDataFactory.user(2L, RoleName.ROLE_USER);

        UserResponse expectedResponse = UserResponse.builder()
                .id(user.getId())
                .status(UserStatus.BLOCKED)
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(admin.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        UserResponse actualResponse = userService.blockUser(user.getId());

        assertEquals(expectedResponse, actualResponse);
        assertEquals(UserStatus.BLOCKED, user.getStatus());
    }

    @Test
    void blockUserShouldThrowExceptionWhenAdminBlocksHimself() {
        Long adminId = 1L;

        when(currentUserProvider.getCurrentUserId()).thenReturn(adminId);

        assertThrows(AdminSelfBlockException.class, () -> userService.blockUser(adminId));
    }

    @Test
    void blockUserShouldThrowExceptionWhenUserAlreadyBlocked() {
        User admin = TestDataFactory.user(1L, RoleName.ROLE_ADMIN);
        User blockedUser = TestDataFactory.blockedUser(2L, RoleName.ROLE_USER);

        when(currentUserProvider.getCurrentUserId()).thenReturn(admin.getId());
        when(userRepository.findByIdWithRoles(blockedUser.getId())).thenReturn(Optional.of(blockedUser));

        assertThrows(UserAlreadyBlockedException.class, () -> userService.blockUser(blockedUser.getId()));
    }
}