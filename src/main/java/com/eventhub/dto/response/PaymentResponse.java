package com.eventhub.dto.response;

import com.eventhub.enums.PaymentStatus;
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
public class PaymentResponse {

    private Long id;

    private Long registrationId;

    private Long userId;
    private String username;
    private String userEmail;

    private Long eventId;
    private String eventTitle;

    private Long ticketTypeId;
    private String ticketTypeName;

    private BigDecimal amount;
    private PaymentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}