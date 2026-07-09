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
public class AdminStatisticsResponse {

    private Long usersCount;
    private Long organizersCount;
    private Long eventsCount;
    private Long registrationsCount;
    private BigDecimal totalRevenue;
    private Long blockedUsersCount;
    private LocalDateTime generatedAt;
}