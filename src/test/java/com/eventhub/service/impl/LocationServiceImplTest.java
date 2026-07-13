package com.eventhub.service.impl;

import com.eventhub.dto.request.LocationCreateRequest;
import com.eventhub.dto.request.LocationUpdateRequest;
import com.eventhub.dto.response.LocationResponse;
import com.eventhub.entity.Location;
import com.eventhub.exception.location.LocationInUseException;
import com.eventhub.exception.location.LocationNotFoundException;
import com.eventhub.mapper.LocationMapper;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.LocationRepository;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationServiceImpl locationService;

    @Test
    void createLocationShouldSaveLocationWithTrimmedFields() {
        LocationCreateRequest request = LocationCreateRequest.builder()
                .country(" Belarus ")
                .city(" Minsk ")
                .address(" Main street 1 ")
                .placeName(" Conference Hall ")
                .build();

        LocationResponse expectedResponse = LocationResponse.builder()
                .id(1L)
                .country("Belarus")
                .city("Minsk")
                .address("Main street 1")
                .placeName("Conference Hall")
                .build();

        when(locationRepository.save(any(Location.class))).thenAnswer(invocation -> {
            Location location = invocation.getArgument(0);
            location.setId(1L);
            return location;
        });
        when(locationMapper.toResponse(any(Location.class))).thenReturn(expectedResponse);

        LocationResponse actualResponse = locationService.createLocation(request);

        assertEquals(expectedResponse, actualResponse);
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void getLocationByIdShouldReturnLocation() {
        Location location = TestDataFactory.location(1L);

        LocationResponse expectedResponse = LocationResponse.builder()
                .id(location.getId())
                .country(location.getCountry())
                .city(location.getCity())
                .address(location.getAddress())
                .placeName(location.getPlaceName())
                .build();

        when(locationRepository.findById(location.getId())).thenReturn(Optional.of(location));
        when(locationMapper.toResponse(location)).thenReturn(expectedResponse);

        LocationResponse actualResponse = locationService.getLocationById(location.getId());

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void getLocationByIdShouldThrowExceptionWhenLocationNotFound() {
        when(locationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class, () -> locationService.getLocationById(99L));
    }

    @Test
    void updateLocationShouldChangeLocationFields() {
        Location location = TestDataFactory.location(1L);

        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .country(" Poland ")
                .city(" Warsaw ")
                .address(" Center 10 ")
                .placeName(" Expo Center ")
                .build();

        LocationResponse expectedResponse = LocationResponse.builder()
                .id(location.getId())
                .country("Poland")
                .city("Warsaw")
                .address("Center 10")
                .placeName("Expo Center")
                .build();

        when(locationRepository.findById(location.getId())).thenReturn(Optional.of(location));
        when(locationMapper.toResponse(location)).thenReturn(expectedResponse);

        LocationResponse actualResponse = locationService.updateLocation(location.getId(), request);

        assertEquals(expectedResponse, actualResponse);
        assertEquals("Poland", location.getCountry());
        assertEquals("Warsaw", location.getCity());
        assertEquals("Center 10", location.getAddress());
        assertEquals("Expo Center", location.getPlaceName());
    }

    @Test
    void deleteLocationShouldDeleteLocationWhenItIsNotUsed() {
        Location location = TestDataFactory.location(1L);

        when(locationRepository.findById(location.getId())).thenReturn(Optional.of(location));
        when(eventRepository.existsByLocationId(location.getId())).thenReturn(false);

        locationService.deleteLocation(location.getId());

        verify(locationRepository).delete(location);
    }

    @Test
    void deleteLocationShouldThrowExceptionWhenLocationIsUsed() {
        Location location = TestDataFactory.location(1L);

        when(locationRepository.findById(location.getId())).thenReturn(Optional.of(location));
        when(eventRepository.existsByLocationId(location.getId())).thenReturn(true);

        assertThrows(LocationInUseException.class, () -> locationService.deleteLocation(location.getId()));

        verify(locationRepository, never()).delete(location);
    }
}