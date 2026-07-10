package com.eventhub.controller;

import com.eventhub.dto.request.OrganizerApplicationReviewRequest;
import com.eventhub.dto.response.OrganizerApplicationResponse;
import com.eventhub.enums.OrganizerApplicationStatus;
import com.eventhub.service.OrganizerApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/organizer-applications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin organizer applications", description = "Admin review of organizer applications")
public class AdminOrganizerApplicationController {

    private final OrganizerApplicationService organizerApplicationService;

    @GetMapping
    public ResponseEntity<Page<OrganizerApplicationResponse>> getApplications(
            @RequestParam(required = false) OrganizerApplicationStatus status,
            @ParameterObject
            @PageableDefault(sort = "createdAt")
            Pageable pageable) {
        return ResponseEntity.ok(organizerApplicationService.getApplications(status, pageable));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<OrganizerApplicationResponse> approveApplication(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) OrganizerApplicationReviewRequest request) {
        return ResponseEntity.ok(organizerApplicationService.approveApplication(id, request));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<OrganizerApplicationResponse> rejectApplication(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) OrganizerApplicationReviewRequest request) {
        return ResponseEntity.ok(organizerApplicationService.rejectApplication(id, request));
    }
}