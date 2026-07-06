package com.eventhub.controller;

import com.eventhub.dto.request.TicketTypeCreateRequest;
import com.eventhub.dto.request.TicketTypeUpdateRequest;
import com.eventhub.dto.response.TicketTypeResponse;
import com.eventhub.service.TicketTypeService;
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
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @PostMapping("/events/{eventId}/ticket-types")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<TicketTypeResponse> createTicketType(
            @PathVariable Long eventId,
            @Valid @RequestBody TicketTypeCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ticketTypeService.createTicketType(eventId, request));
    }

    @GetMapping("/events/{eventId}/ticket-types")
    public ResponseEntity<List<TicketTypeResponse>> getTicketTypesByEvent(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(ticketTypeService.getTicketTypesByEvent(eventId));
    }

    @GetMapping("/ticket-types/{id}")
    public ResponseEntity<TicketTypeResponse> getTicketTypeById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ticketTypeService.getTicketTypeById(id));
    }

    @PutMapping("/ticket-types/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<TicketTypeResponse> updateTicketType(
            @PathVariable Long id,
            @Valid @RequestBody TicketTypeUpdateRequest request) {
        return ResponseEntity.ok(ticketTypeService.updateTicketType(id, request));
    }

    @DeleteMapping("/ticket-types/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<Void> deleteTicketType(
            @PathVariable Long id) {
        ticketTypeService.deleteTicketType(id);

        return ResponseEntity.noContent().build();
    }
}