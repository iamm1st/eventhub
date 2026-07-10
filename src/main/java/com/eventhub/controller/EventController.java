package com.eventhub.controller;

import com.eventhub.dto.request.EventCreateRequest;
import com.eventhub.dto.request.EventUpdateRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.dto.response.EventShortResponse;
import com.eventhub.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event search, filtering and management")
public class EventController {

    private final EventService eventService;

    @Operation(
            summary = "Get events",
            description = "Returns paginated events with optional filtering by category, city and keyword")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Events returned successfully")})
    @GetMapping
    public ResponseEntity<Page<EventShortResponse>> getAllEvents(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword,
            @ParameterObject
            @PageableDefault(sort = "startDate")
            Pageable pageable) {
        return ResponseEntity.ok(eventService.getAllEvents(categoryId, city, keyword, pageable));
    }

    @Operation(summary = "Get event by id", description = "Returns full event information by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event found"),
            @ApiResponse(responseCode = "404", description = "Event not found")})
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @Operation(summary = "Create event", description = "Creates a new event. Available only for organizers")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Organizer role required"),
            @ApiResponse(responseCode = "404", description = "Category or location not found")})
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventService.createEvent(request));
    }

    @Operation(summary = "Update event", description = "Updates an existing event. Available for event owner or admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Business rule conflict")})
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @Operation(summary = "Publish event", description = "Changes event status from DRAFT to PUBLISHED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event published successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Event can't be published")})
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<EventResponse> publishEvent(
            @PathVariable Long id) {
        return ResponseEntity.ok(eventService.publishEvent(id));
    }

    @Operation(summary = "Cancel event", description = "Cancels an event. Available for event owner or admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event cancelled successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Event can't be cancelled")})
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<EventResponse> cancelEvent(
            @PathVariable Long id) {
        return ResponseEntity.ok(eventService.cancelEvent(id));
    }

    @Operation(summary = "Delete event", description = "Deletes an event if business rules allow it")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Event can't be deleted")})
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id) {
        eventService.deleteEvent(id);

        return ResponseEntity.noContent().build();
    }
}