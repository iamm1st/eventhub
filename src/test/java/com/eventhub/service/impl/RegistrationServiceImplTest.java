package com.eventhub.service.impl;

import com.eventhub.dto.request.RegistrationCreateRequest;
import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.Payment;
import com.eventhub.entity.Registration;
import com.eventhub.entity.TicketType;
import com.eventhub.entity.User;
import com.eventhub.enums.PaymentStatus;
import com.eventhub.enums.RegistrationStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.exception.registration.OrganizerOwnEventRegistrationException;
import com.eventhub.exception.registration.RegistrationAlreadyExistsException;
import com.eventhub.exception.registration.TicketUnavailableException;
import com.eventhub.mapper.RegistrationMapper;
import com.eventhub.repository.PaymentRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.TicketTypeRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private RegistrationMapper registrationMapper;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Test
    void buyTicketShouldCreateRegistrationAndPayment() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(20L, event, 2);

        RegistrationResponse expectedResponse = RegistrationResponse.builder()
                .id(30L)
                .status(RegistrationStatus.ACTIVE)
                .paymentStatus(PaymentStatus.PAID)
                .build();

        RegistrationCreateRequest request = RegistrationCreateRequest.builder()
                .ticketTypeId(ticketType.getId())
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(ticketTypeRepository.findByIdForUpdate(ticketType.getId())).thenReturn(Optional.of(ticketType));
        when(registrationRepository.existsByUserIdAndEventIdAndStatus(
                user.getId(),
                event.getId(),
                RegistrationStatus.ACTIVE)).thenReturn(false);
        when(registrationRepository.save(any(Registration.class))).thenAnswer(invocation -> {
            Registration registration = invocation.getArgument(0);
            registration.setId(30L);
            return registration;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(40L);
            return payment;
        });
        when(registrationMapper.toResponse(any(Registration.class), any(Payment.class))).thenReturn(expectedResponse);

        RegistrationResponse actualResponse = registrationService.buyTicket(request);

        assertEquals(expectedResponse, actualResponse);
        assertEquals(1, ticketType.getAvailableQuantity());

        verify(registrationRepository).save(any(Registration.class));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void buyTicketShouldThrowExceptionWhenNoTicketsAvailable() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(20L, event, 0);

        RegistrationCreateRequest request = RegistrationCreateRequest.builder()
                .ticketTypeId(ticketType.getId())
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(ticketTypeRepository.findByIdForUpdate(ticketType.getId())).thenReturn(Optional.of(ticketType));
        when(registrationRepository.existsByUserIdAndEventIdAndStatus(
                user.getId(),
                event.getId(),
                RegistrationStatus.ACTIVE)).thenReturn(false);

        assertThrows(TicketUnavailableException.class, () -> registrationService.buyTicket(request));

        verify(registrationRepository, never()).save(any(Registration.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void buyTicketShouldThrowExceptionWhenUserBuysOwnEvent() {
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(20L, event, 2);

        RegistrationCreateRequest request = RegistrationCreateRequest.builder()
                .ticketTypeId(ticketType.getId())
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());
        when(userRepository.findByIdWithRoles(organizer.getId())).thenReturn(Optional.of(organizer));
        when(ticketTypeRepository.findByIdForUpdate(ticketType.getId())).thenReturn(Optional.of(ticketType));

        assertThrows(OrganizerOwnEventRegistrationException.class, () -> registrationService.buyTicket(request));

        verify(registrationRepository, never()).save(any(Registration.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void buyTicketShouldThrowExceptionWhenUserAlreadyRegistered() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(20L, event, 2);

        RegistrationCreateRequest request = RegistrationCreateRequest.builder()
                .ticketTypeId(ticketType.getId())
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(userRepository.findByIdWithRoles(user.getId())).thenReturn(Optional.of(user));
        when(ticketTypeRepository.findByIdForUpdate(ticketType.getId())).thenReturn(Optional.of(ticketType));
        when(registrationRepository.existsByUserIdAndEventIdAndStatus(
                user.getId(),
                event.getId(),
                RegistrationStatus.ACTIVE)).thenReturn(true);

        assertThrows(RegistrationAlreadyExistsException.class, () -> registrationService.buyTicket(request));

        verify(registrationRepository, never()).save(any(Registration.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void cancelRegistrationShouldCancelRegistrationAndRefundPayment() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(20L, event, 1);
        Registration registration = TestDataFactory.activeRegistration(30L, user, event, ticketType);
        Payment payment = TestDataFactory.paidPayment(40L, registration);

        RegistrationResponse expectedResponse = RegistrationResponse.builder()
                .id(registration.getId())
                .status(RegistrationStatus.CANCELLED)
                .paymentStatus(PaymentStatus.REFUNDED)
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(user.getId());
        when(registrationRepository.findById(registration.getId())).thenReturn(Optional.of(registration));
        when(paymentRepository.findByRegistrationId(registration.getId())).thenReturn(Optional.of(payment));
        when(registrationMapper.toResponse(registration, payment)).thenReturn(expectedResponse);

        RegistrationResponse actualResponse = registrationService.cancelRegistration(registration.getId());

        assertEquals(expectedResponse, actualResponse);
        assertEquals(RegistrationStatus.CANCELLED, registration.getStatus());
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        assertEquals(2, ticketType.getAvailableQuantity());
    }
}