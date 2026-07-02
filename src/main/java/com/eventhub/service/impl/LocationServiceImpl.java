package com.eventhub.service.impl;

import com.eventhub.dto.request.LocationCreateRequest;
import com.eventhub.dto.request.LocationUpdateRequest;
import com.eventhub.dto.response.LocationResponse;
import com.eventhub.entity.Location;
import com.eventhub.exception.LocationInUseException;
import com.eventhub.exception.LocationNotFoundException;
import com.eventhub.mapper.LocationMapper;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.LocationRepository;
import com.eventhub.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final LocationMapper locationMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<LocationResponse> getAllLocations(Pageable pageable) {
        return locationRepository.findAll(pageable)
                .map(locationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse getLocationById(Long id) {
        Location location = findLocationById(id);

        return locationMapper.toResponse(location);
    }

    @Override
    @Transactional
    public LocationResponse createLocation(LocationCreateRequest request) {
        Location location = Location.builder()
                .country(normalizeText(request.getCountry()))
                .city(normalizeText(request.getCity()))
                .address(normalizeText(request.getAddress()))
                .placeName(normalizeText(request.getPlaceName()))
                .build();

        Location savedLocation = locationRepository.save(location);

        return locationMapper.toResponse(savedLocation);
    }

    @Override
    @Transactional
    public LocationResponse updateLocation(Long id, LocationUpdateRequest request) {
        Location location = findLocationById(id);

        location.setCountry(normalizeText(request.getCountry()));
        location.setCity(normalizeText(request.getCity()));
        location.setAddress(normalizeText(request.getAddress()));
        location.setPlaceName(normalizeText(request.getPlaceName()));

        return locationMapper.toResponse(location);
    }

    @Override
    @Transactional
    public void deleteLocation(Long id) {
        Location location = findLocationById(id);

        if (eventRepository.existsByLocationId(id)) {
            throw new LocationInUseException(id);
        }

        locationRepository.delete(location);
    }

    private Location findLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));
    }

    private String normalizeText(String value) {
        return value.trim();
    }
}