package com.eventhub.service.impl;

import com.eventhub.dto.request.ReviewCreateRequest;
import com.eventhub.dto.response.ReviewResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.Review;
import com.eventhub.entity.User;
import com.eventhub.enums.RegistrationStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.exception.event.EventNotFinishedException;
import com.eventhub.exception.review.ReviewAlreadyExistsException;
import com.eventhub.mapper.ReviewMapper;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.ReviewRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void createReviewShouldSaveReviewAndRecalculateRating() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.finishedEvent(10L, organizer);

        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .rating(5)
                .comment(" Great event ")
                .build();

        ReviewResponse expectedResponse = ReviewResponse.builder()
                .id(100L)
                .rating(5)
                .comment("Great event")
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventIdAndStatus(
                user.getId(),
                event.getId(),
                RegistrationStatus.ACTIVE)).thenReturn(true);
        when(reviewRepository.existsByUserIdAndEventId(user.getId(), event.getId())).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(100L);
            return review;
        });
        when(reviewRepository.calculateAverageRatingByEventId(event.getId())).thenReturn(5.0);
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(expectedResponse);

        ReviewResponse actualResponse = reviewService.createReview(event.getId(), request);

        assertEquals(expectedResponse, actualResponse);
        assertEquals(BigDecimal.valueOf(5.00).setScale(2), event.getRating());

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReviewShouldThrowExceptionWhenEventIsNotFinished() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);

        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .rating(5)
                .comment("Good")
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        assertThrows(EventNotFinishedException.class, () -> reviewService.createReview(event.getId(), request));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReviewShouldThrowExceptionWhenReviewAlreadyExists() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.finishedEvent(10L, organizer);

        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .rating(5)
                .comment("Good")
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventIdAndStatus(
                user.getId(),
                event.getId(),
                RegistrationStatus.ACTIVE)).thenReturn(true);
        when(reviewRepository.existsByUserIdAndEventId(user.getId(), event.getId())).thenReturn(true);

        assertThrows(ReviewAlreadyExistsException.class, () -> reviewService.createReview(event.getId(), request));

        verify(reviewRepository, never()).save(any(Review.class));
    }
}