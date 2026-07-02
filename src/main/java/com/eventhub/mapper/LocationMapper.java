package com.eventhub.mapper;

import com.eventhub.dto.response.LocationResponse;
import com.eventhub.entity.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public LocationResponse toResponse(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .country(location.getCountry())
                .city(location.getCity())
                .address(location.getAddress())
                .placeName(location.getPlaceName())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .build();
    }
}