package com.eventhub.controller;

import com.eventhub.dto.request.RegistrationCreateRequest;
import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Registrations", description = "Ticket purchase and registration cancellation")
public class RegistrationController {

    private final RegistrationService registrationService;

    @Operation(
            summary = "Buy ticket",
            description = "Creates active registration, decreases available ticket quantity and creates paid payment")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket purchased successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Ticket type not found"),
            @ApiResponse(responseCode = "409", description = "Ticket unavailable or user already registered")})
    @PostMapping("/registrations")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RegistrationResponse> buyTicket(
            @Valid @RequestBody RegistrationCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registrationService.buyTicket(request));
    }

    @Operation(
            summary = "Cancel registration",
            description = "Cancels active registration, increases available ticket quantity and refunds payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registration cancelled successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Registration not found"),
            @ApiResponse(responseCode = "409", description = "Registration can't be cancelled")})
    @PatchMapping("/registrations/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RegistrationResponse> cancelRegistration(
            @PathVariable Long id) {
        return ResponseEntity.ok(registrationService.cancelRegistration(id));
    }

    @Operation(summary = "Get my registrations", description = "Returns registrations of the current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registrations returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")})
    @GetMapping("/registrations/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<RegistrationResponse>> getMyRegistrations() {
        return ResponseEntity.ok(registrationService.getMyRegistrations());
    }

    @Operation(
            summary = "Get event registrations",
            description = "Returns participants of an event. Available for event organizer or admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registrations returned successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Event not found")})
    @GetMapping("/events/{eventId}/registrations")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<List<RegistrationResponse>> getRegistrationsByEvent(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(registrationService.getRegistrationsByEvent(eventId));
    }
}