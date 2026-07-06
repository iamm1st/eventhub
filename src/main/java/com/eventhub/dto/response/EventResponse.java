package com.eventhub.dto.response;

import com.eventhub.enums.EventStatus;
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
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer capacity;
    private EventStatus status;
    private BigDecimal rating;

    private Long organizerId;
    private String organizerUsername;
    private String organizerEmail;

    private Long categoryId;
    private String categoryName;

    private Long locationId;
    private String country;
    private String city;
    private String address;
    private String placeName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}