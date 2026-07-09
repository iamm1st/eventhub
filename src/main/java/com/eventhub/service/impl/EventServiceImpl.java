package com.eventhub.service.impl;

import com.eventhub.aspect.LogAction;
import com.eventhub.dto.request.EventCreateRequest;
import com.eventhub.dto.request.EventUpdateRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.dto.response.EventShortResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventCategory;
import com.eventhub.entity.Location;
import com.eventhub.entity.User;
import com.eventhub.enums.EventStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.enums.UserStatus;
import com.eventhub.exception.auth.UserBlockedException;
import com.eventhub.exception.category.CategoryNotFoundException;
import com.eventhub.exception.event.EventAccessDeniedException;
import com.eventhub.exception.event.EventAlreadyCancelledException;
import com.eventhub.exception.event.EventAlreadyStartedException;
import com.eventhub.exception.event.EventCannotBeDeletedException;
import com.eventhub.exception.event.EventCannotBePublishedException;
import com.eventhub.exception.event.EventCannotBeUpdatedException;
import com.eventhub.exception.event.EventCapacityBelowTicketQuantityException;
import com.eventhub.exception.event.EventNotFoundException;
import com.eventhub.exception.event.InvalidEventDatesException;
import com.eventhub.exception.location.LocationNotFoundException;
import com.eventhub.exception.user.UserNotFoundException;
import com.eventhub.mapper.EventMapper;
import com.eventhub.repository.EventCategoryRepository;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.LocationRepository;
import com.eventhub.repository.TicketTypeRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.service.EventService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository eventCategoryRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final CurrentUserProvider currentUserProvider;
    private final EventMapper eventMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<EventShortResponse> getAllEvents(
            Long categoryId,
            String city,
            String keyword,
            Pageable pageable) {
        return eventRepository.findAll(buildEventSpecification(categoryId, city, keyword), pageable)
                .map(eventMapper::toShortResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        Event event = findEventById(id);

        return eventMapper.toResponse(event);
    }

    @Override
    @LogAction(action = "CREATE_EVENT", entityType = "EVENT")
    @Transactional
    public EventResponse createEvent(EventCreateRequest request) {
        validateEventDates(request.getStartDate(), request.getEndDate());

        Long currentUserId = currentUserProvider.getCurrentUserId();
        User organizer = findUserByIdWithRoles(currentUserId);

        validateOrganizer(organizer);

        EventCategory category = findCategoryById(request.getCategoryId());
        Location location = findLocationById(request.getLocationId());

        Event event = Event.builder()
                .title(normalizeText(request.getTitle()))
                .description(normalizeText(request.getDescription()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .capacity(request.getCapacity())
                .status(EventStatus.DRAFT)
                .organizer(organizer)
                .category(category)
                .location(location)
                .build();

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toResponse(savedEvent);
    }

    @Override
    @LogAction(action = "UPDATE_EVENT", entityType = "EVENT", entityIdArgIndex = 0)
    @Transactional
    public EventResponse updateEvent(Long id, EventUpdateRequest request) {
        validateEventDates(request.getStartDate(), request.getEndDate());

        Event event = findEventById(id);
        checkCanManageEvent(event);
        checkEventCanBeUpdated(event);
        validateCapacityIsNotBelowTicketQuantity(event.getId(), request.getCapacity());

        EventCategory category = findCategoryById(request.getCategoryId());
        Location location = findLocationById(request.getLocationId());

        event.setTitle(normalizeText(request.getTitle()));
        event.setDescription(normalizeText(request.getDescription()));
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setCapacity(request.getCapacity());
        event.setCategory(category);
        event.setLocation(location);

        return eventMapper.toResponse(event);
    }

    @Override
    @LogAction(action = "PUBLISH_EVENT", entityType = "EVENT", entityIdArgIndex = 0)
    @Transactional
    public EventResponse publishEvent(Long id) {
        Event event = findEventById(id);
        checkCanManageEvent(event);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new EventCannotBePublishedException(id);
        }

        checkEventNotStarted(event);
        validateEventDates(event.getStartDate(), event.getEndDate());

        event.setStatus(EventStatus.PUBLISHED);

        return eventMapper.toResponse(event);
    }

    @Override
    @LogAction(action = "CANCEL_EVENT", entityType = "EVENT", entityIdArgIndex = 0)
    @Transactional
    public EventResponse cancelEvent(Long id) {
        Event event = findEventById(id);
        checkCanManageEvent(event);

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new EventAlreadyCancelledException(id);
        }

        checkEventNotStarted(event);

        event.setStatus(EventStatus.CANCELLED);

        return eventMapper.toResponse(event);
    }

    @Override
    @LogAction(action = "DELETE_EVENT", entityType = "EVENT", entityIdArgIndex = 0, useReturnedId = false)
    @Transactional
    public void deleteEvent(Long id) {
        Event event = findEventById(id);
        checkCanManageEvent(event);

        if (event.getStatus() == EventStatus.PUBLISHED) {
            throw new EventCannotBeDeletedException(id);
        }

        eventRepository.delete(event);
    }

    private Specification<Event> buildEventSpecification(
            Long categoryId,
            String city,
            String keyword) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            if (city != null && !city.isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("location").get("city")),
                        city.trim().toLowerCase()));
            }

            if (keyword != null && !keyword.isBlank()) {
                String searchPattern = "%" + keyword.trim().toLowerCase() + "%";

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
    }

    private User findUserByIdWithRoles(Long id) {
        return userRepository.findByIdWithRoles(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    private EventCategory findCategoryById(Long id) {
        return eventCategoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }

    private Location findLocationById(Long id) {
        return locationRepository.findById(id).orElseThrow(() -> new LocationNotFoundException(id));
    }

    private void validateOrganizer(User organizer) {
        if (organizer.getStatus() == UserStatus.BLOCKED) {
            throw new UserBlockedException(organizer.getEmail());
        }

        boolean isOrganizer = organizer.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ROLE_ORGANIZER);

        if (!isOrganizer) {
            throw new EventAccessDeniedException();
        }
    }

    private void checkCanManageEvent(Event event) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        if (event.getOrganizer().getId().equals(currentUserId) || isCurrentUserAdmin()) {
            return;
        }

        throw new EventAccessDeniedException(event.getId());
    }

    private boolean isCurrentUserAdmin() {
        return currentUserProvider.getCurrentUserDetails()
                .getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals(RoleName.ROLE_ADMIN.name()));
    }

    private void checkEventCanBeUpdated(Event event) {
        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.FINISHED) {
            throw new EventCannotBeUpdatedException(event.getId());
        }

        checkEventNotStarted(event);
    }

    private void checkEventNotStarted(Event event) {
        if (!event.getStartDate().isAfter(LocalDateTime.now())) {
            throw new EventAlreadyStartedException(event.getId());
        }
    }

    private void validateEventDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (!endDate.isAfter(startDate)) {
            throw new InvalidEventDatesException();
        }
    }

    private void validateCapacityIsNotBelowTicketQuantity(Long eventId, Integer newCapacity) {
        Long totalTicketQuantity = ticketTypeRepository.sumTotalQuantityByEventId(eventId);

        if (totalTicketQuantity > newCapacity) {
            throw new EventCapacityBelowTicketQuantityException(eventId);
        }
    }

    private String normalizeText(String value) {
        return value.trim();
    }
}