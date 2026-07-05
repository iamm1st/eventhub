package com.eventhub.dto.response;

import com.eventhub.enums.OrganizerApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerApplicationResponse {

    private Long id;

    private Long userId;
    private String username;
    private String userEmail;

    private String organizationName;
    private String contactEmail;
    private String contactPhone;
    private String description;
    private String websiteUrl;

    private OrganizerApplicationStatus status;
    private String adminComment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;
}