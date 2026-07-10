package com.eventhub.controller;

import com.eventhub.dto.request.LocationCreateRequest;
import com.eventhub.dto.request.LocationUpdateRequest;
import com.eventhub.dto.response.LocationResponse;
import com.eventhub.service.LocationService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Event location management")
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "Get locations", description = "Returns paginated event locations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locations returned successfully")})
    @GetMapping
    public ResponseEntity<Page<LocationResponse>> getAllLocations(
            @ParameterObject
            @PageableDefault(sort = "city")
            Pageable pageable) {
        return ResponseEntity.ok(locationService.getAllLocations(pageable));
    }

    @Operation(summary = "Get location by id", description = "Returns location by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location found"),
            @ApiResponse(responseCode = "404", description = "Location not found")})
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getLocationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }

    @Operation(summary = "Create location", description = "Creates location. Organizer or admin only")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Location created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<LocationResponse> createLocation(
            @Valid @RequestBody LocationCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(locationService.createLocation(request));
    }

    @Operation(summary = "Update location", description = "Updates location. Organizer or admin only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Location not found")})
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationUpdateRequest request) {
        return ResponseEntity.ok(locationService.updateLocation(id, request));
    }

    @Operation(summary = "Delete location", description = "Deletes location if it isn't used by events. Admin only")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Location deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Admin role required"),
            @ApiResponse(responseCode = "404", description = "Location not found"),
            @ApiResponse(responseCode = "409", description = "Location is used by events")})
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable Long id) {
        locationService.deleteLocation(id);

        return ResponseEntity.noContent().build();
    }
}