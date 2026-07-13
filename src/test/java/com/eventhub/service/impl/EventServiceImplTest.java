package com.eventhub.service.impl;

import com.eventhub.dto.request.EventCreateRequest;
import com.eventhub.dto.request.EventUpdateRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventCategory;
import com.eventhub.entity.Location;
import com.eventhub.entity.User;
import com.eventhub.enums.EventStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.exception.event.EventAccessDeniedException;
import com.eventhub.exception.event.EventCannotBePublishedException;
import com.eventhub.exception.event.EventNotFoundException;
import com.eventhub.exception.event.InvalidEventDatesException;
import com.eventhub.mapper.EventMapper;
import com.eventhub.repository.EventCategoryRepository;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.LocationRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventCategoryRepository eventCategoryRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void createEventShouldCreateDraftEventForOrganizer() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        EventCategory category = TestDataFactory.category(2L);
        Location location = TestDataFactory.location(3L);

        EventCreateRequest request = EventCreateRequest.builder()
                .title(" Java Conference ")
                .description(" Conference about Java ")
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(10).plusHours(2))
                .capacity(100)
                .categoryId(category.getId())
                .locationId(location.getId())
                .build();

        EventResponse expectedResponse = EventResponse.builder().id(10L).title("Java Conference").status(EventStatus.DRAFT).build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());
        when(userRepository.findByIdWithRoles(organizer.getId())).thenReturn(Optional.of(organizer));
        when(eventCategoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(locationRepository.findById(location.getId())).thenReturn(Optional.of(location));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setId(10L);
            return event;
        });
        when(eventMapper.toResponse(any(Event.class))).thenReturn(expectedResponse);

        EventResponse actualResponse = eventService.createEvent(request);

        assertEquals(expectedResponse, actualResponse);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEventShouldThrowExceptionWhenDatesAreInvalid() {
        EventCreateRequest request = EventCreateRequest.builder()
                .title("Java Conference")
                .description("Description")
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(9))
                .capacity(100)
                .categoryId(1L)
                .locationId(1L)
                .build();

        assertThrows(InvalidEventDatesException.class, () -> eventService.createEvent(request));

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEventShouldThrowExceptionWhenUserIsNotOrganizer() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);

        EventCreateRequest request = EventCreateRequest.builder()
                .title("Java Conference")
                .description("Description")
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(10).plusHours(2))
                .capacity(100)
                .categoryId(1L)
                .locationId(1L)
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));

        assertThrows(EventAccessDeniedException.class, () -> eventService.createEvent(request));

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void getEventByIdShouldReturnEvent() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);

        EventResponse expectedResponse = EventResponse.builder().id(event.getId()).title(event.getTitle()).build();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(eventMapper.toResponse(event)).thenReturn(expectedResponse);

        EventResponse actualResponse = eventService.getEventById(event.getId());

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void getEventByIdShouldThrowExceptionWhenEventNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventService.getEventById(99L));
    }

    @Test
    void publishEventShouldChangeStatusToPublished() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futureDraftEvent(10L, organizer);

        EventResponse expectedResponse = EventResponse.builder().id(event.getId()).status(EventStatus.PUBLISHED).build();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());
        when(eventMapper.toResponse(event)).thenReturn(expectedResponse);

        EventResponse actualResponse = eventService.publishEvent(event.getId());

        assertEquals(expectedResponse, actualResponse);
        assertEquals(EventStatus.PUBLISHED, event.getStatus());
    }

    @Test
    void publishEventShouldThrowExceptionWhenEventIsNotDraft() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());

        assertThrows(EventCannotBePublishedException.class, () -> eventService.publishEvent(event.getId()));
    }

    @Test
    void updateEventShouldThrowExceptionWhenCurrentUserIsNotOwner() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        User anotherUser = TestDataFactory.user(2L, RoleName.ROLE_USER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);

        EventUpdateRequest request = EventUpdateRequest.builder()
                .title("Updated")
                .description("Updated")
                .startDate(LocalDateTime.now().plusDays(15))
                .endDate(LocalDateTime.now().plusDays(15).plusHours(2))
                .capacity(100)
                .categoryId(1L)
                .locationId(1L)
                .build();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(currentUserProvider.getCurrentUserId()).thenReturn(anotherUser.getId());
        when(currentUserProvider.getCurrentUserDetails()).thenReturn(TestDataFactory.userDetails(anotherUser));

        assertThrows(EventAccessDeniedException.class, () -> eventService.updateEvent(event.getId(), request));
    }
}