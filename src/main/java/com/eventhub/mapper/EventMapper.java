package com.eventhub.mapper;

import com.eventhub.dto.response.EventResponse;
import com.eventhub.dto.response.EventShortResponse;
import com.eventhub.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public EventShortResponse toShortResponse(Event event) {
        return EventShortResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .capacity(event.getCapacity())
                .status(event.getStatus())
                .rating(event.getRating())
                .categoryName(event.getCategory().getName())
                .city(event.getLocation().getCity())
                .placeName(event.getLocation().getPlaceName())
                .build();
    }

    public EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .capacity(event.getCapacity())
                .status(event.getStatus())
                .rating(event.getRating())
                .organizerId(event.getOrganizer().getId())
                .organizerUsername(event.getOrganizer().getUsername())
                .organizerEmail(event.getOrganizer().getEmail())
                .categoryId(event.getCategory().getId())
                .categoryName(event.getCategory().getName())
                .locationId(event.getLocation().getId())
                .country(event.getLocation().getCountry())
                .city(event.getLocation().getCity())
                .address(event.getLocation().getAddress())
                .placeName(event.getLocation().getPlaceName())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}