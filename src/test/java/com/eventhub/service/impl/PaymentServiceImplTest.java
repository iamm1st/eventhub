package com.eventhub.service.impl;

import com.eventhub.dto.response.PaymentResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.Payment;
import com.eventhub.entity.Registration;
import com.eventhub.entity.TicketType;
import com.eventhub.entity.User;
import com.eventhub.enums.PaymentStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.exception.payment.PaymentNotFoundException;
import com.eventhub.mapper.PaymentMapper;
import com.eventhub.repository.PaymentRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void getPaymentByIdShouldReturnPayment() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(20L, event, 5);
        Registration registration = TestDataFactory.activeRegistration(30L, user, event, ticketType);
        Payment payment = TestDataFactory.paidPayment(40L, registration);

        PaymentResponse expectedResponse = PaymentResponse.builder()
                .id(payment.getId())
                .amount(BigDecimal.valueOf(50))
                .status(PaymentStatus.PAID)
                .build();

        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(expectedResponse);

        PaymentResponse actualResponse = paymentService.getPaymentById(payment.getId());

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void getPaymentByIdShouldThrowExceptionWhenPaymentNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentById(99L));
    }

    @Test
    void getOrganizerPaymentsShouldReturnCurrentOrganizerPayments() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(20L, event, 5);
        Registration registration = TestDataFactory.activeRegistration(30L, user, event, ticketType);
        Payment payment = TestDataFactory.paidPayment(40L, registration);

        PaymentResponse response = PaymentResponse.builder()
                .id(payment.getId())
                .status(PaymentStatus.PAID)
                .build();

        PageRequest pageable = PageRequest.of(0, 10);

        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());
        when(paymentRepository.findByRegistrationEventOrganizerIdOrderByCreatedAtDesc(organizer.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(payment)));
        when(paymentMapper.toResponse(payment)).thenReturn(response);

        assertEquals(1, paymentService.getOrganizerPayments(pageable).getTotalElements());
    }
}