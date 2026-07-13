package com.eventhub.service.impl;

import com.eventhub.dto.response.AuditLogResponse;
import com.eventhub.entity.AuditLog;
import com.eventhub.mapper.AuditLogMapper;
import com.eventhub.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Test
    void saveLogShouldSaveAuditLog() {
        auditLogService.saveLog(
                "admin@eventhub.com",
                "BLOCK_USER",
                "USER",
                2L,
                true,
                null,
                25L);

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void getAuditLogsShouldReturnMappedAuditLogs() {
        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .username("admin@eventhub.com")
                .action("BLOCK_USER")
                .entityType("USER")
                .entityId(2L)
                .success(true)
                .executionTimeMs(25L)
                .build();

        AuditLogResponse response = AuditLogResponse.builder()
                .id(auditLog.getId())
                .username(auditLog.getUsername())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .success(true)
                .executionTimeMs(25L)
                .build();

        PageRequest pageable = PageRequest.of(0, 10);

        when(auditLogRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(auditLog)));
        when(auditLogMapper.toResponse(auditLog)).thenReturn(response);

        assertEquals(1, auditLogService.getAuditLogs(pageable).getTotalElements());
    }
}