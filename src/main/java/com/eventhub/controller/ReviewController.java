package com.eventhub.controller;

import com.eventhub.dto.request.ReviewCreateRequest;
import com.eventhub.dto.request.ReviewUpdateRequest;
import com.eventhub.dto.response.ReviewResponse;
import com.eventhub.service.ReviewService;
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
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/events/{eventId}/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long eventId,
            @Valid @RequestBody ReviewCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewService.createReview(eventId, request));
    }

    @GetMapping("/events/{eventId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getReviewsByEvent(
            @PathVariable Long eventId,
            @ParameterObject
            @PageableDefault(sort = "createdAt")
            Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviewsByEvent(eventId, pageable));
    }

    @PutMapping("/reviews/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(id, request));
    }

    @DeleteMapping("/reviews/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id) {
        reviewService.deleteReview(id);

        return ResponseEntity.noContent().build();
    }
}