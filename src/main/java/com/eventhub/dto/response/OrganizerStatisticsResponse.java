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
public class OrganizerStatisticsResponse {

    private Long organizerId;
    private Long eventsCount;
    private Long soldTickets;
    private BigDecimal totalRevenue;
    private BigDecimal averageRating;
    private String mostPopularEventTitle;
    private Long mostPopularEventTicketsSold;
    private LocalDateTime generatedAt;
}