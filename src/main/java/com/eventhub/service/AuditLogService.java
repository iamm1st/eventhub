package com.eventhub.service;

import com.eventhub.dto.response.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    void saveLog(
            String username,
            String action,
            String entityType,
            Long entityId,
            boolean success,
            String errorMessage,
            Long executionTimeMs);

    Page<AuditLogResponse> getAuditLogs(Pageable pageable);
}