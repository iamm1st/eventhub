package com.eventhub.service;

import com.eventhub.dto.request.LocationCreateRequest;
import com.eventhub.dto.request.LocationUpdateRequest;
import com.eventhub.dto.response.LocationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LocationService {

    Page<LocationResponse> getAllLocations(Pageable pageable);

    LocationResponse getLocationById(Long id);

    LocationResponse createLocation(LocationCreateRequest request);

    LocationResponse updateLocation(Long id, LocationUpdateRequest request);

    void deleteLocation(Long id);
}