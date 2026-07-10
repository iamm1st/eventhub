package com.eventhub.controller;

import com.eventhub.dto.request.TicketTypeCreateRequest;
import com.eventhub.dto.request.TicketTypeUpdateRequest;
import com.eventhub.dto.response.TicketTypeResponse;
import com.eventhub.service.TicketTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Ticket types", description = "Ticket type management for events")
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @Operation(summary = "Create ticket type", description = "Creates ticket type for event")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket type created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Ticket quantity exceeds event capacity")})
    @PostMapping("/events/{eventId}/ticket-types")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<TicketTypeResponse> createTicketType(
            @PathVariable Long eventId,
            @Valid @RequestBody TicketTypeCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ticketTypeService.createTicketType(eventId, request));
    }

    @Operation(summary = "Get event ticket types", description = "Returns ticket types for event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket types returned successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")})
    @GetMapping("/events/{eventId}/ticket-types")
    public ResponseEntity<List<TicketTypeResponse>> getTicketTypesByEvent(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(ticketTypeService.getTicketTypesByEvent(eventId));
    }

    @Operation(summary = "Get ticket type by id", description = "Returns ticket type by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket type found"),
            @ApiResponse(responseCode = "404", description = "Ticket type not found")})
    @GetMapping("/ticket-types/{id}")
    public ResponseEntity<TicketTypeResponse> getTicketTypeById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ticketTypeService.getTicketTypeById(id));
    }

    @Operation(summary = "Update ticket type", description = "Updates ticket type if current user can manage event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket type updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Ticket type not found"),
            @ApiResponse(responseCode = "409", description = "Business rule conflict")})
    @PutMapping("/ticket-types/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<TicketTypeResponse> updateTicketType(
            @PathVariable Long id,
            @Valid @RequestBody TicketTypeUpdateRequest request) {
        return ResponseEntity.ok(ticketTypeService.updateTicketType(id, request));
    }

    @Operation(summary = "Delete ticket type", description = "Deletes ticket type if it has no active registrations")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ticket type deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Ticket type not found"),
            @ApiResponse(responseCode = "409", description = "Ticket type has active registrations")})
    @DeleteMapping("/ticket-types/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<Void> deleteTicketType(
            @PathVariable Long id) {
        ticketTypeService.deleteTicketType(id);

        return ResponseEntity.noContent().build();
    }
}