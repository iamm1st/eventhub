package com.eventhub.mapper;

import com.eventhub.dto.response.OrganizerApplicationResponse;
import com.eventhub.entity.OrganizerApplication;
import org.springframework.stereotype.Component;

@Component
public class OrganizerApplicationMapper {

    public OrganizerApplicationResponse toResponse(OrganizerApplication application) {
        return OrganizerApplicationResponse.builder()
                .id(application.getId())
                .userId(application.getUser().getId())
                .username(application.getUser().getUsername())
                .userEmail(application.getUser().getEmail())
                .organizationName(application.getOrganizationName())
                .contactEmail(application.getContactEmail())
                .contactPhone(application.getContactPhone())
                .description(application.getDescription())
                .websiteUrl(application.getWebsiteUrl())
                .status(application.getStatus())
                .adminComment(application.getAdminComment())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .reviewedAt(application.getReviewedAt())
                .build();
    }
}