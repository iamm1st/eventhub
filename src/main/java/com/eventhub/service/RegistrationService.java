package com.eventhub.service;

import com.eventhub.dto.request.RegistrationCreateRequest;
import com.eventhub.dto.response.RegistrationResponse;

import java.util.List;

public interface RegistrationService {

    RegistrationResponse buyTicket(RegistrationCreateRequest request);

    RegistrationResponse cancelRegistration(Long id);

    List<RegistrationResponse> getMyRegistrations();

    List<RegistrationResponse> getRegistrationsByEvent(Long eventId);
}