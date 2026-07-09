package com.eventhub.mapper;

import com.eventhub.dto.response.AuditLogResponse;
import com.eventhub.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .username(auditLog.getUsername())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .success(auditLog.isSuccess())
                .errorMessage(auditLog.getErrorMessage())
                .executionTimeMs(auditLog.getExecutionTimeMs())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}