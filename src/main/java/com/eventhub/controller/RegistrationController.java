package com.eventhub.controller;

import com.eventhub.dto.request.RegistrationCreateRequest;
import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/registrations")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RegistrationResponse> buyTicket(
            @Valid @RequestBody RegistrationCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registrationService.buyTicket(request));
    }

    @PatchMapping("/registrations/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RegistrationResponse> cancelRegistration(
            @PathVariable Long id) {
        return ResponseEntity.ok(registrationService.cancelRegistration(id));
    }

    @GetMapping("/registrations/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<RegistrationResponse>> getMyRegistrations() {
        return ResponseEntity.ok(registrationService.getMyRegistrations());
    }

    @GetMapping("/events/{eventId}/registrations")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<List<RegistrationResponse>> getRegistrationsByEvent(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(registrationService.getRegistrationsByEvent(eventId));
    }
}