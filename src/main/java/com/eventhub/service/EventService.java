package com.eventhub.service;

import com.eventhub.dto.request.EventCreateRequest;
import com.eventhub.dto.request.EventUpdateRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.dto.response.EventShortResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {

    Page<EventShortResponse> getAllEvents(
            Long categoryId,
            String city,
            String keyword,
            Pageable pageable);

    EventResponse getEventById(Long id);

    EventResponse createEvent(EventCreateRequest request);

    EventResponse updateEvent(Long id, EventUpdateRequest request);

    EventResponse publishEvent(Long id);

    EventResponse cancelEvent(Long id);

    void deleteEvent(Long id);
}