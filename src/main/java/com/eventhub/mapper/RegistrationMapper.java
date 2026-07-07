package com.eventhub.mapper;

import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.entity.Payment;
import com.eventhub.entity.Registration;
import org.springframework.stereotype.Component;

@Component
public class RegistrationMapper {

    public RegistrationResponse toResponse(Registration registration, Payment payment) {
        return RegistrationResponse.builder()
                .id(registration.getId())
                .userId(registration.getUser().getId())
                .username(registration.getUser().getUsername())
                .userEmail(registration.getUser().getEmail())
                .eventId(registration.getEvent().getId())
                .eventTitle(registration.getEvent().getTitle())
                .ticketTypeId(registration.getTicketType().getId())
                .ticketTypeName(registration.getTicketType().getName())
                .price(registration.getTicketType().getPrice())
                .status(registration.getStatus())
                .paymentId(payment.getId())
                .paymentAmount(payment.getAmount())
                .paymentStatus(payment.getStatus())
                .registrationDate(registration.getRegistrationDate())
                .cancelledAt(registration.getCancelledAt())
                .createdAt(registration.getCreatedAt())
                .updatedAt(registration.getUpdatedAt())
                .build();
    }
}