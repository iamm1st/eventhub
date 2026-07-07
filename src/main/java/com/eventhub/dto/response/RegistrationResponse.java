package com.eventhub.dto.response;

import com.eventhub.enums.PaymentStatus;
import com.eventhub.enums.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {

    private Long id;

    private Long userId;
    private String username;
    private String userEmail;

    private Long eventId;
    private String eventTitle;

    private Long ticketTypeId;
    private String ticketTypeName;

    private BigDecimal price;
    private RegistrationStatus status;

    private Long paymentId;
    private BigDecimal paymentAmount;
    private PaymentStatus paymentStatus;

    private LocalDateTime registrationDate;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}