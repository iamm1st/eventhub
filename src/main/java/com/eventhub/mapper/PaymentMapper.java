package com.eventhub.mapper;

import com.eventhub.dto.response.PaymentResponse;
import com.eventhub.entity.Payment;
import com.eventhub.entity.Registration;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        Registration registration = payment.getRegistration();

        return PaymentResponse.builder()
                .id(payment.getId())
                .registrationId(registration.getId())
                .userId(registration.getUser().getId())
                .username(registration.getUser().getUsername())
                .userEmail(registration.getUser().getEmail())
                .eventId(registration.getEvent().getId())
                .eventTitle(registration.getEvent().getTitle())
                .ticketTypeId(registration.getTicketType().getId())
                .ticketTypeName(registration.getTicketType().getName())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}