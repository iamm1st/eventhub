package com.eventhub.controller;

import com.eventhub.dto.request.ReviewCreateRequest;
import com.eventhub.dto.request.ReviewUpdateRequest;
import com.eventhub.dto.response.ReviewResponse;
import com.eventhub.service.ReviewService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Event reviews and rating management")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "Create review",
            description = "Creates review for finished event if current user has active registration")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Review created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User can't review this event"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Review already exists or event is not finished")})
    @PostMapping("/events/{eventId}/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long eventId,
            @Valid @RequestBody ReviewCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewService.createReview(eventId, request));
    }

    @Operation(summary = "Get event reviews", description = "Returns paginated reviews for event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reviews returned successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")})
    @GetMapping("/events/{eventId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByEvent(
            @PathVariable Long eventId,
            @ParameterObject
            @PageableDefault(sort = "createdAt")
            Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsByEvent(eventId, pageable));
    }

    @Operation(summary = "Update review", description = "Updates current user's review and recalculates event rating")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Review not found")})
    @PutMapping("/reviews/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(id, request));
    }

    @Operation(summary = "Delete review", description = "Deletes review and recalculates event rating")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Review not found")})
    @DeleteMapping("/reviews/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id) {
        reviewService.deleteReview(id);

        return ResponseEntity.noContent().build();
    }
}