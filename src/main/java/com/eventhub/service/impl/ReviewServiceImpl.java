package com.eventhub.service.impl;

import com.eventhub.aspect.LogAction;
import com.eventhub.dto.request.ReviewCreateRequest;
import com.eventhub.dto.request.ReviewUpdateRequest;
import com.eventhub.dto.response.ReviewResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.Review;
import com.eventhub.entity.User;
import com.eventhub.enums.EventStatus;
import com.eventhub.enums.RegistrationStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.enums.UserStatus;
import com.eventhub.exception.auth.UserBlockedException;
import com.eventhub.exception.event.EventNotFinishedException;
import com.eventhub.exception.event.EventNotFoundException;
import com.eventhub.exception.review.ReviewAccessDeniedException;
import com.eventhub.exception.review.ReviewAlreadyExistsException;
import com.eventhub.exception.review.ReviewNotAllowedException;
import com.eventhub.exception.review.ReviewNotFoundException;
import com.eventhub.exception.user.UserNotFoundException;
import com.eventhub.mapper.ReviewMapper;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.ReviewRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final int RATING_SCALE = 2;

    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ReviewMapper reviewMapper;

    @Override
    @LogAction(action = "CREATE_REVIEW", entityType = "REVIEW")
    @Transactional
    public ReviewResponse createReview(Long eventId, ReviewCreateRequest request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        User currentUser = findUserById(currentUserId);

        if (currentUser.getStatus() == UserStatus.BLOCKED) {
            throw new UserBlockedException(currentUser.getEmail());
        }

        Event event = findEventById(eventId);

        if (event.getStatus() != EventStatus.FINISHED) {
            throw new EventNotFinishedException(event.getId());
        }

        if (!registrationRepository.existsByUserIdAndEventIdAndStatus(
                currentUser.getId(),
                event.getId(),
                RegistrationStatus.ACTIVE)) {
            throw new ReviewNotAllowedException(event.getId());
        }

        if (reviewRepository.existsByUserIdAndEventId(currentUser.getId(), event.getId())) {
            throw new ReviewAlreadyExistsException(event.getId());
        }

        Review review = Review.builder()
                .user(currentUser)
                .event(event)
                .rating(request.getRating())
                .comment(normalizeNullableText(request.getComment()))
                .build();

        Review savedReview = reviewRepository.save(review);

        recalculateEventRating(event);

        return reviewMapper.toResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByEvent(Long eventId, Pageable pageable) {
        findEventById(eventId);

        return reviewRepository.findByEventIdOrderByCreatedAtDesc(eventId, pageable).map(reviewMapper::toResponse);
    }

    @Override
    @LogAction(action = "UPDATE_REVIEW", entityType = "REVIEW", entityIdArgIndex = 0)
    @Transactional
    public ReviewResponse updateReview(Long id, ReviewUpdateRequest request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        User currentUser = findUserById(currentUserId);

        if (currentUser.getStatus() == UserStatus.BLOCKED) {
            throw new UserBlockedException(currentUser.getEmail());
        }

        Review review = findReviewById(id);

        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new ReviewAccessDeniedException(id);
        }

        review.setRating(request.getRating());
        review.setComment(normalizeNullableText(request.getComment()));

        recalculateEventRating(review.getEvent());

        return reviewMapper.toResponse(review);
    }

    @Override
    @LogAction(action = "DELETE_REVIEW", entityType = "REVIEW", entityIdArgIndex = 0, useReturnedId = false)
    @Transactional
    public void deleteReview(Long id) {
        Review review = findReviewById(id);

        if (!canDeleteReview(review)) {
            throw new ReviewAccessDeniedException(id);
        }

        Event event = review.getEvent();

        reviewRepository.delete(review);
        // to make sure the deletion of the review is accurately stored in the db before recalculating the average rating
        reviewRepository.flush();

        recalculateEventRating(event);
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
    }

    private Review findReviewById(Long id) {
        return reviewRepository.findById(id).orElseThrow(() -> new ReviewNotFoundException(id));
    }

    private User findUserById(Long id) {
        return userRepository.findByIdWithRoles(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    private boolean canDeleteReview(Review review) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        return review.getUser().getId().equals(currentUserId) || isCurrentUserAdmin();
    }

    private boolean isCurrentUserAdmin() {
        return currentUserProvider.getCurrentUserDetails()
                .getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals(RoleName.ROLE_ADMIN.name()));
    }

    private void recalculateEventRating(Event event) {
        Double averageRating = reviewRepository.calculateAverageRatingByEventId(event.getId());

        BigDecimal newRating = BigDecimal.valueOf(averageRating).setScale(RATING_SCALE, RoundingMode.HALF_UP);

        event.setRating(newRating);
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}