package com.eventhub.service.impl;

import com.eventhub.dto.request.TicketTypeCreateRequest;
import com.eventhub.dto.request.TicketTypeUpdateRequest;
import com.eventhub.dto.response.TicketTypeResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.TicketType;
import com.eventhub.enums.EventStatus;
import com.eventhub.enums.RegistrationStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.exception.event.EventAccessDeniedException;
import com.eventhub.exception.event.EventAlreadyStartedException;
import com.eventhub.exception.event.EventNotFoundException;
import com.eventhub.exception.ticket.TicketQuantityBelowSoldException;
import com.eventhub.exception.ticket.TicketQuantityExceededException;
import com.eventhub.exception.ticket.TicketTypeAlreadyExistsException;
import com.eventhub.exception.ticket.TicketTypeCannotBeManagedException;
import com.eventhub.exception.ticket.TicketTypeInUseException;
import com.eventhub.exception.ticket.TicketTypeNotFoundException;
import com.eventhub.mapper.TicketTypeMapper;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.TicketTypeRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final CurrentUserProvider currentUserProvider;
    private final TicketTypeMapper ticketTypeMapper;

    @Override
    @Transactional
    public TicketTypeResponse createTicketType(Long eventId, TicketTypeCreateRequest request) {
        Event event = findEventById(eventId);

        checkCanManageEvent(event);
        checkEventAllowsTicketChanges(event);

        String normalizedName = normalizeText(request.getName());

        if (ticketTypeRepository.existsByEventIdAndNameIgnoreCase(eventId, normalizedName)) {
            throw new TicketTypeAlreadyExistsException(normalizedName);
        }

        validateTicketQuantityLimit(
                eventId,
                null,
                request.getTotalQuantity(),
                event.getCapacity());

        TicketType ticketType = TicketType.builder()
                .event(event)
                .name(normalizedName)
                .price(request.getPrice())
                .totalQuantity(request.getTotalQuantity())
                .availableQuantity(request.getTotalQuantity())
                .build();

        TicketType savedTicketType = ticketTypeRepository.save(ticketType);

        return ticketTypeMapper.toResponse(savedTicketType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketTypeResponse> getTicketTypesByEvent(Long eventId) {
        findEventById(eventId);

        return ticketTypeRepository.findByEventIdOrderByPriceAsc(eventId)
                .stream()
                .map(ticketTypeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketTypeResponse getTicketTypeById(Long id) {
        TicketType ticketType = findTicketTypeById(id);

        return ticketTypeMapper.toResponse(ticketType);
    }

    @Override
    @Transactional
    public TicketTypeResponse updateTicketType(Long id, TicketTypeUpdateRequest request) {
        TicketType ticketType = findTicketTypeById(id);
        Event event = ticketType.getEvent();

        checkCanManageEvent(event);
        checkEventAllowsTicketChanges(event);

        String normalizedName = normalizeText(request.getName());

        if (ticketTypeRepository.existsByEventIdAndNameIgnoreCaseAndIdNot(
                event.getId(),
                normalizedName,
                ticketType.getId())) {
            throw new TicketTypeAlreadyExistsException(normalizedName);
        }

        Integer soldQuantity = ticketType.getTotalQuantity() - ticketType.getAvailableQuantity();

        if (request.getTotalQuantity() < soldQuantity) {
            throw new TicketQuantityBelowSoldException(ticketType.getId(), soldQuantity);
        }

        validateTicketQuantityLimit(
                event.getId(),
                ticketType.getId(),
                request.getTotalQuantity(),
                event.getCapacity());

        int quantityDifference = request.getTotalQuantity() - ticketType.getTotalQuantity();

        ticketType.setName(normalizedName);
        ticketType.setPrice(request.getPrice());
        ticketType.setTotalQuantity(request.getTotalQuantity());
        ticketType.setAvailableQuantity(ticketType.getAvailableQuantity() + quantityDifference);

        return ticketTypeMapper.toResponse(ticketType);
    }

    @Override
    @Transactional
    public void deleteTicketType(Long id) {
        TicketType ticketType = findTicketTypeById(id);

        checkCanManageEvent(ticketType.getEvent());
        checkEventAllowsTicketChanges(ticketType.getEvent());

        if (registrationRepository.existsByTicketTypeIdAndStatus(id, RegistrationStatus.ACTIVE)) {
            throw new TicketTypeInUseException(id);
        }

        ticketTypeRepository.delete(ticketType);
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
    }

    private TicketType findTicketTypeById(Long id) {
        return ticketTypeRepository.findById(id).orElseThrow(() -> new TicketTypeNotFoundException(id));
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

    private void checkEventAllowsTicketChanges(Event event) {
        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.FINISHED) {
            throw new TicketTypeCannotBeManagedException(event.getId());
        }

        if (!event.getStartDate().isAfter(LocalDateTime.now())) {
            throw new EventAlreadyStartedException(event.getId());
        }
    }

    private void validateTicketQuantityLimit(
            Long eventId,
            Long excludedTicketTypeId,
            Integer newTicketQuantity,
            Integer eventCapacity) {
        Long existingTicketQuantity;

        if (excludedTicketTypeId == null) {
            existingTicketQuantity = ticketTypeRepository.sumTotalQuantityByEventId(eventId);
        } else {
            existingTicketQuantity = ticketTypeRepository.sumTotalQuantityByEventIdExcludingTicketType(eventId, excludedTicketTypeId);
        }

        if (existingTicketQuantity + newTicketQuantity > eventCapacity) {
            throw new TicketQuantityExceededException(eventCapacity);
        }
    }

    private String normalizeText(String value) {
        return value.trim();
    }
}