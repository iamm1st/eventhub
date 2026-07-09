package com.eventhub.service.impl;

import com.eventhub.dto.response.AuditLogResponse;
import com.eventhub.entity.AuditLog;
import com.eventhub.mapper.AuditLogMapper;
import com.eventhub.repository.AuditLogRepository;
import com.eventhub.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    // audit log is saved in a separate transaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(
            String username,
            String action,
            String entityType,
            Long entityId,
            boolean success,
            String errorMessage,
            Long executionTimeMs) {
        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .success(success)
                .errorMessage(errorMessage)
                .executionTimeMs(executionTimeMs)
                .build();

        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(auditLogMapper::toResponse);
    }
}