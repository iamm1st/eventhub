package com.eventhub.dto.response;

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
public class AuditLogResponse {

    private Long id;
    private String username;
    private String action;
    private String entityType;
    private Long entityId;
    private boolean success;
    private String errorMessage;
    private Long executionTimeMs;
    private LocalDateTime createdAt;
}