package com.eventhub.service.impl;

import com.eventhub.aspect.LogAction;
import com.eventhub.dto.request.RegistrationCreateRequest;
import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.Payment;
import com.eventhub.entity.Registration;
import com.eventhub.entity.TicketType;
import com.eventhub.entity.User;
import com.eventhub.enums.EventStatus;
import com.eventhub.enums.PaymentStatus;
import com.eventhub.enums.RegistrationStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.enums.UserStatus;
import com.eventhub.exception.auth.UserBlockedException;
import com.eventhub.exception.event.EventAccessDeniedException;
import com.eventhub.exception.event.EventAlreadyCancelledException;
import com.eventhub.exception.event.EventAlreadyStartedException;
import com.eventhub.exception.event.EventNotFoundException;
import com.eventhub.exception.event.EventNotPublishedException;
import com.eventhub.exception.payment.PaymentNotFoundException;
import com.eventhub.exception.registration.OrganizerOwnEventRegistrationException;
import com.eventhub.exception.registration.RegistrationAccessDeniedException;
import com.eventhub.exception.registration.RegistrationAlreadyExistsException;
import com.eventhub.exception.registration.RegistrationCannotBeCancelledException;
import com.eventhub.exception.registration.RegistrationNotFoundException;
import com.eventhub.exception.registration.TicketUnavailableException;
import com.eventhub.exception.ticket.TicketTypeNotFoundException;
import com.eventhub.exception.user.UserNotFoundException;
import com.eventhub.mapper.RegistrationMapper;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.PaymentRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.TicketTypeRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final RegistrationMapper registrationMapper;

    @Override
    @LogAction(action = "BUY_TICKET", entityType = "REGISTRATION")
    @Transactional
    public RegistrationResponse buyTicket(RegistrationCreateRequest request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        User currentUser = findUserById(currentUserId);

        if (currentUser.getStatus() == UserStatus.BLOCKED) {
            throw new UserBlockedException(currentUser.getEmail());
        }

        TicketType ticketType = ticketTypeRepository.findByIdForUpdate(request.getTicketTypeId())
                .orElseThrow(() -> new TicketTypeNotFoundException(request.getTicketTypeId()));

        Event event = ticketType.getEvent();

        validateEventForPurchase(event);
        validateUserCanBuyTicket(currentUser, event);

        if (registrationRepository.existsByUserIdAndEventIdAndStatus(
                currentUser.getId(),
                event.getId(),
                RegistrationStatus.ACTIVE)) {
            throw new RegistrationAlreadyExistsException(event.getId());
        }

        if (ticketType.getAvailableQuantity() <= 0) {
            throw new TicketUnavailableException(ticketType.getId());
        }

        ticketType.setAvailableQuantity(ticketType.getAvailableQuantity() - 1);

        Registration registration = Registration.builder()
                .user(currentUser)
                .event(event)
                .ticketType(ticketType)
                .status(RegistrationStatus.ACTIVE)
                .build();

        Registration savedRegistration = registrationRepository.save(registration);

        Payment payment = Payment.builder()
                .registration(savedRegistration)
                .amount(ticketType.getPrice())
                .status(PaymentStatus.PAID)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return registrationMapper.toResponse(savedRegistration, savedPayment);
    }

    @Override
    @LogAction(action = "CANCEL_REGISTRATION", entityType = "REGISTRATION", entityIdArgIndex = 0)
    @Transactional
    public RegistrationResponse cancelRegistration(Long id) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        Registration registration = findRegistrationById(id);
        Event event = registration.getEvent();

        if (!registration.getUser().getId().equals(currentUserId)) {
            throw new RegistrationAccessDeniedException(id);
        }

        if (registration.getStatus() != RegistrationStatus.ACTIVE) {
            throw new RegistrationCannotBeCancelledException(id);
        }

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new EventAlreadyCancelledException(event.getId());
        }

        checkEventNotStarted(event);

        TicketType ticketType = registration.getTicketType();
        ticketType.setAvailableQuantity(ticketType.getAvailableQuantity() + 1);

        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setCancelledAt(LocalDateTime.now());

        Payment payment = paymentRepository.findByRegistrationId(registration.getId())
                .orElseThrow(() -> new PaymentNotFoundException(registration.getId()));

        payment.setStatus(PaymentStatus.REFUNDED);

        return registrationMapper.toResponse(registration, payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getMyRegistrations() {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        return registrationRepository.findByUserIdOrderByRegistrationDateDesc(currentUserId)
                .stream()
                .map(this::toResponseWithPayment)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getRegistrationsByEvent(Long eventId) {
        Event event = findEventById(eventId);
        checkCanManageEvent(event);

        return registrationRepository.findByEventIdOrderByRegistrationDateDesc(eventId)
                .stream()
                .map(this::toResponseWithPayment)
                .toList();
    }

    private void validateEventForPurchase(Event event) {
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new EventAlreadyCancelledException(event.getId());
        }

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new EventNotPublishedException(event.getId());
        }

        checkEventNotStarted(event);
    }

    private void validateUserCanBuyTicket(User user, Event event) {
        if (event.getOrganizer().getId().equals(user.getId())) {
            throw new OrganizerOwnEventRegistrationException(event.getId());
        }
    }

    private void checkEventNotStarted(Event event) {
        if (!event.getStartDate().isAfter(LocalDateTime.now())) {
            throw new EventAlreadyStartedException(event.getId());
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

    private User findUserById(Long id) {
        return userRepository.findByIdWithRoles(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
    }

    private Registration findRegistrationById(Long id) {
        return registrationRepository.findById(id).orElseThrow(() -> new RegistrationNotFoundException(id));
    }

    private RegistrationResponse toResponseWithPayment(Registration registration) {
        Payment payment = paymentRepository.findByRegistrationId(registration.getId())
                .orElseThrow(() -> new PaymentNotFoundException(registration.getId()));

        return registrationMapper.toResponse(registration, payment);
    }
}