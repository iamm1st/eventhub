package com.eventhub.controller;

import com.eventhub.dto.request.OrganizerApplicationCreateRequest;
import com.eventhub.dto.response.OrganizerApplicationResponse;
import com.eventhub.service.OrganizerApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizer-applications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class OrganizerApplicationController {

    private final OrganizerApplicationService organizerApplicationService;

    @PostMapping
    public ResponseEntity<OrganizerApplicationResponse> createApplication(
            @Valid @RequestBody OrganizerApplicationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizerApplicationService.createApplication(request));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<OrganizerApplicationResponse>> getMyApplications(
            @ParameterObject
            @PageableDefault(sort = "createdAt")
            Pageable pageable) {
        return ResponseEntity.ok(organizerApplicationService.getMyApplications(pageable));
    }
}