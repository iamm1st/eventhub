package com.eventhub.service;

import com.eventhub.dto.request.TicketTypeCreateRequest;
import com.eventhub.dto.request.TicketTypeUpdateRequest;
import com.eventhub.dto.response.TicketTypeResponse;

import java.util.List;

public interface TicketTypeService {

    TicketTypeResponse createTicketType(Long eventId, TicketTypeCreateRequest request);

    List<TicketTypeResponse> getTicketTypesByEvent(Long eventId);

    TicketTypeResponse getTicketTypeById(Long id);

    TicketTypeResponse updateTicketType(Long id, TicketTypeUpdateRequest request);

    void deleteTicketType(Long id);
}