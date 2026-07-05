package com.eventhub.service;

import com.eventhub.dto.request.UserUpdateRequest;
import com.eventhub.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse getCurrentUser();

    UserResponse updateCurrentUser(UserUpdateRequest request);

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse getUserById(Long id);

    UserResponse blockUser(Long id);

    UserResponse unblockUser(Long id);
}