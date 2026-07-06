package com.eventhub.dto.response;

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
public class TicketTypeResponse {

    private Long id;

    private Long eventId;
    private String eventTitle;

    private String name;
    private BigDecimal price;
    private Integer totalQuantity;
    private Integer availableQuantity;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}