package com.eventhub.service;

import com.eventhub.dto.request.OrganizerApplicationCreateRequest;
import com.eventhub.dto.request.OrganizerApplicationReviewRequest;
import com.eventhub.dto.response.OrganizerApplicationResponse;
import com.eventhub.enums.OrganizerApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizerApplicationService {

    OrganizerApplicationResponse createApplication(OrganizerApplicationCreateRequest request);

    Page<OrganizerApplicationResponse> getMyApplications(Pageable pageable);

    Page<OrganizerApplicationResponse> getApplications(OrganizerApplicationStatus status, Pageable pageable);

    OrganizerApplicationResponse approveApplication(Long id, OrganizerApplicationReviewRequest request);

    OrganizerApplicationResponse rejectApplication(Long id, OrganizerApplicationReviewRequest request);
}