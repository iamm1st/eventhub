package com.eventhub.service.impl;

import com.eventhub.dto.request.EventCreateRequest;
import com.eventhub.dto.request.EventUpdateRequest;
import com.eventhub.dto.request.RegistrationCreateRequest;
import com.eventhub.dto.request.ReviewCreateRequest;
import com.eventhub.dto.request.ReviewUpdateRequest;
import com.eventhub.dto.request.UserUpdateRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.dto.response.ReviewResponse;
import com.eventhub.dto.response.UserResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventCategory;
import com.eventhub.entity.Location;
import com.eventhub.entity.Payment;
import com.eventhub.entity.Registration;
import com.eventhub.entity.Review;
import com.eventhub.entity.TicketType;
import com.eventhub.entity.User;
import com.eventhub.enums.EventStatus;
import com.eventhub.enums.RegistrationStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.enums.UserStatus;
import com.eventhub.exception.auth.EmailAlreadyExistsException;
import com.eventhub.exception.auth.UserBlockedException;
import com.eventhub.exception.auth.UsernameAlreadyExistsException;
import com.eventhub.exception.event.EventAlreadyCancelledException;
import com.eventhub.exception.event.EventAlreadyStartedException;
import com.eventhub.exception.event.EventCannotBeDeletedException;
import com.eventhub.exception.event.EventCapacityBelowTicketQuantityException;
import com.eventhub.exception.event.EventNotPublishedException;
import com.eventhub.exception.payment.PaymentNotFoundException;
import com.eventhub.exception.registration.RegistrationAccessDeniedException;
import com.eventhub.exception.review.ReviewAccessDeniedException;
import com.eventhub.exception.review.ReviewNotAllowedException;
import com.eventhub.exception.user.UserAlreadyActiveException;
import com.eventhub.mapper.EventMapper;
import com.eventhub.mapper.RegistrationMapper;
import com.eventhub.mapper.ReviewMapper;
import com.eventhub.mapper.UserMapper;
import com.eventhub.repository.EventCategoryRepository;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.LocationRepository;
import com.eventhub.repository.PaymentRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.ReviewRepository;
import com.eventhub.repository.TicketTypeRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceEdgeCaseCoverageTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventCategoryRepository eventCategoryRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private RegistrationMapper registrationMapper;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private UserMapper userMapper;

    private EventServiceImpl eventService;
    private RegistrationServiceImpl registrationService;
    private ReviewServiceImpl reviewService;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(
                eventRepository,
                eventCategoryRepository,
                locationRepository,
                userRepository,
                ticketTypeRepository,
                currentUserProvider,
                eventMapper);

        registrationService = new RegistrationServiceImpl(
                registrationRepository,
                ticketTypeRepository,
                paymentRepository,
                eventRepository,
                userRepository,
                currentUserProvider,
                registrationMapper);

        reviewService = new ReviewServiceImpl(
                reviewRepository,
                eventRepository,
                registrationRepository,
                userRepository,
                currentUserProvider,
                reviewMapper);

        userService = new UserServiceImpl(userRepository, currentUserProvider, userMapper);
    }

    @Test
    void updateEventShouldUpdateEventWhenCurrentUserIsAdmin() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        User admin = TestDataFactory.user(2L, RoleName.ROLE_ADMIN);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        EventCategory category = TestDataFactory.category(3L);
        Location location = TestDataFactory.location(4L);

        EventUpdateRequest request = EventUpdateRequest.builder()
                .title(" Updated title ")
                .description(" Updated description ")
                .startDate(LocalDateTime.now().plusDays(20))
                .endDate(LocalDateTime.now().plusDays(20).plusHours(2))
                .capacity(120)
                .categoryId(category.getId())
                .locationId(location.getId())
                .build();

        EventResponse response = EventResponse.builder()
                .id(event.getId())
                .title("Updated title")
                .build();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(currentUserProvider.getCurrentUserId()).thenReturn(admin.getId());
        when(currentUserProvider.getCurrentUserDetails()).thenReturn(TestDataFactory.userDetails(admin));
        when(ticketTypeRepository.sumTotalQuantityByEventId(event.getId())).thenReturn(10L);
        when(eventCategoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(locationRepository.findById(location.getId())).thenReturn(Optional.of(location));
        when(eventMapper.toResponse(event)).thenReturn(response);

        EventResponse actualResponse = eventService.updateEvent(event.getId(), request);

        assertEquals(response, actualResponse);
        assertEquals("Updated title", event.getTitle());
        assertEquals("Updated description", event.getDescription());
        assertEquals(120, event.getCapacity());
        assertEquals(category, event.getCategory());
        assertEquals(location, event.getLocation());
    }

    @Test
    void updateEventShouldThrowExceptionWhenCapacityIsLowerThanTicketQuantity() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);

        EventUpdateRequest request = EventUpdateRequest.builder()
                .title("Updated")
                .description("Updated")
                .startDate(LocalDateTime.now().plusDays(20))
                .endDate(LocalDateTime.now().plusDays(20).plusHours(2))
                .capacity(5)
                .categoryId(1L)
                .locationId(1L)
                .build();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());
        when(ticketTypeRepository.sumTotalQuantityByEventId(event.getId())).thenReturn(10L);

        assertThrows(EventCapacityBelowTicketQuantityException.class, () -> eventService.updateEvent(event.getId(), request));
    }

    @Test
    void cancelEventShouldSetStatusToCancelled() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);

        EventResponse response = EventResponse.builder().id(event.getId()).status(EventStatus.CANCELLED).build();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());
        when(eventMapper.toResponse(event)).thenReturn(response);

        EventResponse actualResponse = eventService.cancelEvent(event.getId());

        assertEquals(response, actualResponse);
        assertEquals(EventStatus.CANCELLED, event.getStatus());
    }

    @Test
    void cancelEventShouldThrowExceptionWhenEventAlreadyCancelled() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        event.setStatus(EventStatus.CANCELLED);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());

        assertThrows(EventAlreadyCancelledException.class, () -> eventService.cancelEvent(event.getId()));
    }

    @Test
    void deleteEventShouldDeleteDraftEvent() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futureDraftEvent(10L, organizer);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());

        eventService.deleteEvent(event.getId());

        verify(eventRepository).delete(event);
    }

    @Test
    void deleteEventShouldThrowExceptionWhenEventIsPublished() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());

        assertThrows(EventCannotBeDeletedException.class, () -> eventService.deleteEvent(event.getId()));

        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    void createEventShouldThrowExceptionWhenOrganizerIsBlocked() {
        User organizer = TestDataFactory.blockedUser(1L, RoleName.ROLE_ORGANIZER);

        EventCreateRequest request = EventCreateRequest.builder()
                .title("Java Conference")
                .description("Description")
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(10).plusHours(2))
                .capacity(100)
                .categoryId(1L)
                .locationId(1L)
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());
        when(userRepository.findByIdWithRoles(organizer.getId())).thenReturn(Optional.of(organizer));

        assertThrows(UserBlockedException.class, () -> eventService.createEvent(request));

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void buyTicketShouldThrowExceptionWhenEventIsCancelled() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        event.setStatus(EventStatus.CANCELLED);
        TicketType ticketType = TestDataFactory.ticketType(20L, event, 2);

        RegistrationCreateRequest request = RegistrationCreateRequest.builder().ticketTypeId(ticketType.getId()).build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(ticketTypeRepository.findByIdForUpdate(ticketType.getId())).thenReturn(Optional.of(ticketType));

        assertThrows(EventAlreadyCancelledException.class, () -> registrationService.buyTicket(request));
    }

    @Test
    void buyTicketShouldThrowExceptionWhenEventIsNotPublished() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futureDraftEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(20L, event, 2);

        RegistrationCreateRequest request = RegistrationCreateRequest.builder()
                .ticketTypeId(ticketType.getId())
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(ticketTypeRepository.findByIdForUpdate(ticketType.getId())).thenReturn(Optional.of(ticketType));

        assertThrows(EventNotPublishedException.class, () -> registrationService.buyTicket(request));
    }

    @Test
    void buyTicketShouldThrowExceptionWhenEventAlreadyStarted() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.finishedEvent(10L, organizer);
        event.setStatus(EventStatus.PUBLISHED);
        TicketType ticketType = TestDataFactory.ticketType(20L, event, 2);

        RegistrationCreateRequest request = RegistrationCreateRequest.builder().ticketTypeId(ticketType.getId()).build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(ticketTypeRepository.findByIdForUpdate(ticketType.getId())).thenReturn(Optional.of(ticketType));

        assertThrows(EventAlreadyStartedException.class, () -> registrationService.buyTicket(request));
    }

    @Test
    void cancelRegistrationShouldThrowExceptionWhenRegistrationBelongsToAnotherUser() {
        User owner = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User anotherUser = TestDataFactory.user(2L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(3L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(20L, event, 1);
        Registration registration = TestDataFactory.activeRegistration(30L, owner, event, ticketType);

        when(currentUserProvider.getCurrentUserId()).thenReturn(anotherUser.getId());
        when(registrationRepository.findById(registration.getId())).thenReturn(Optional.of(registration));

        assertThrows(RegistrationAccessDeniedException.class, () -> registrationService.cancelRegistration(registration.getId()));
    }

    @Test
    void cancelRegistrationShouldThrowExceptionWhenPaymentNotFound() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(20L, event, 1);
        Registration registration = TestDataFactory.activeRegistration(30L, user, event, ticketType);

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(registrationRepository.findById(registration.getId())).thenReturn(Optional.of(registration));
        when(paymentRepository.findByRegistrationId(registration.getId())).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> registrationService.cancelRegistration(registration.getId()));
    }

    @Test
    void getMyRegistrationsShouldReturnMappedRegistrationsWithPayments() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(20L, event, 1);
        Registration registration = TestDataFactory.activeRegistration(30L, user, event, ticketType);
        Payment payment = TestDataFactory.paidPayment(40L, registration);
        RegistrationResponse response = RegistrationResponse.builder().id(registration.getId()).build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(registrationRepository.findByUserIdOrderByRegistrationDateDesc(user.getId())).thenReturn(List.of(registration));
        when(paymentRepository.findByRegistrationId(registration.getId())).thenReturn(Optional.of(payment));
        when(registrationMapper.toResponse(registration, payment)).thenReturn(response);

        List<RegistrationResponse> result = registrationService.getMyRegistrations();

        assertEquals(List.of(response), result);
    }

    @Test
    void getReviewsByEventShouldReturnMappedReviews() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        User user = TestDataFactory.user(2L, RoleName.ROLE_USER);
        Event event = TestDataFactory.finishedEvent(10L, organizer);
        Review review = TestDataFactory.review(30L, user, event);
        ReviewResponse response = ReviewResponse.builder().id(review.getId()).build();
        PageRequest pageable = PageRequest.of(0, 10);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(reviewRepository.findByEventIdOrderByCreatedAtDesc(event.getId(), pageable)).thenReturn(new PageImpl<>(List.of(review)));
        when(reviewMapper.toResponse(review)).thenReturn(response);

        assertEquals(List.of(response), reviewService.getReviewsByEvent(event.getId(), pageable).getContent());
    }

    @Test
    void createReviewShouldThrowExceptionWhenUserHasNoActiveRegistration() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.finishedEvent(10L, organizer);

        ReviewCreateRequest request = ReviewCreateRequest.builder().rating(5).comment("Good").build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(registrationRepository.existsByUserIdAndEventIdAndStatus(user.getId(), event.getId(), RegistrationStatus.ACTIVE)).thenReturn(false);

        assertThrows(ReviewNotAllowedException.class, () -> reviewService.createReview(event.getId(), request));
    }

    @Test
    void updateReviewShouldUpdateOwnReviewAndRecalculateRating() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.finishedEvent(10L, organizer);
        Review review = TestDataFactory.review(30L, user, event);

        ReviewUpdateRequest request = ReviewUpdateRequest.builder().rating(4).comment(" Updated comment ").build();

        ReviewResponse response = ReviewResponse.builder().id(review.getId()).rating(4).comment("Updated comment").build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(reviewRepository.calculateAverageRatingByEventId(event.getId())).thenReturn(4.0);
        when(reviewMapper.toResponse(review)).thenReturn(response);

        ReviewResponse result = reviewService.updateReview(review.getId(), request);

        assertEquals(response, result);
        assertEquals(4, review.getRating());
        assertEquals("Updated comment", review.getComment());
    }

    @Test
    void deleteReviewShouldDeleteOwnReviewAndRecalculateRating() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.finishedEvent(10L, organizer);
        Review review = TestDataFactory.review(30L, user, event);

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(reviewRepository.calculateAverageRatingByEventId(event.getId())).thenReturn(0.0);

        reviewService.deleteReview(review.getId());

        verify(reviewRepository).delete(review);
        verify(reviewRepository).flush();
        assertEquals("0.00", event.getRating().toString());
    }

    @Test
    void deleteReviewShouldThrowExceptionWhenCurrentUserIsNotOwnerOrAdmin() {
        User owner = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User anotherUser = TestDataFactory.user(2L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(3L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.finishedEvent(10L, organizer);
        Review review = TestDataFactory.review(30L, owner, event);

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(currentUserProvider.getCurrentUserId()).thenReturn(anotherUser.getId());
        when(currentUserProvider.getCurrentUserDetails()).thenReturn(TestDataFactory.userDetails(anotherUser));

        assertThrows(ReviewAccessDeniedException.class, () -> reviewService.deleteReview(review.getId()));

        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    void getCurrentUserShouldReturnCurrentUser() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        UserResponse response = UserResponse.builder().id(user.getId()).email(user.getEmail()).build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.getCurrentUser();

        assertEquals(response, result);
    }

    @Test
    void updateCurrentUserShouldUpdateUsernameAndEmail() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);

        UserUpdateRequest request = UserUpdateRequest.builder().username(" updated ").email(" updated@mail.ru ").build();

        UserResponse response = UserResponse.builder().id(user.getId()).username("updated").email("updated@mail.ru").build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameIgnoreCaseAndIdNot("updated", user.getId())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("updated@mail.ru", user.getId())).thenReturn(false);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.updateCurrentUser(request);

        assertEquals(response, result);
        assertEquals("updated", user.getUsername());
        assertEquals("updated@mail.ru", user.getEmail());
    }

    @Test
    void updateCurrentUserShouldThrowExceptionWhenUsernameAlreadyExists() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);

        UserUpdateRequest request = UserUpdateRequest.builder().username(" existing ").email("new@mail.ru").build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameIgnoreCaseAndIdNot("existing", user.getId())).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.updateCurrentUser(request));
    }

    @Test
    void updateCurrentUserShouldThrowExceptionWhenEmailAlreadyExists() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);

        UserUpdateRequest request = UserUpdateRequest.builder().username("updated").email(" existing@mail.ru ").build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameIgnoreCaseAndIdNot("updated", user.getId())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("existing@mail.ru", user.getId())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.updateCurrentUser(request));
    }

    @Test
    void unblockUserShouldSetUserStatusToActive() {
        User blockedUser = TestDataFactory.blockedUser(2L, RoleName.ROLE_USER);

        UserResponse response = UserResponse.builder().id(blockedUser.getId()).status(UserStatus.ACTIVE).build();

        when(userRepository.findByIdWithRoles(blockedUser.getId())).thenReturn(Optional.of(blockedUser));
        when(userMapper.toResponse(blockedUser)).thenReturn(response);

        UserResponse result = userService.unblockUser(blockedUser.getId());

        assertEquals(response, result);
        assertEquals(UserStatus.ACTIVE, blockedUser.getStatus());
    }

    @Test
    void unblockUserShouldThrowExceptionWhenUserAlreadyActive() {
        User user = TestDataFactory.user(2L, RoleName.ROLE_USER);

        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyActiveException.class, () -> userService.unblockUser(user.getId()));
    }
}