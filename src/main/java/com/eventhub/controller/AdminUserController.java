package com.eventhub.controller;

import com.eventhub.dto.response.UserResponse;
import com.eventhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @ParameterObject
            @PageableDefault(sort = "createdAt")
            Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<UserResponse> blockUser(
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.blockUser(id));
    }

    @PatchMapping("/{id}/unblock")
    public ResponseEntity<UserResponse> unblockUser(
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.unblockUser(id));
    }
}