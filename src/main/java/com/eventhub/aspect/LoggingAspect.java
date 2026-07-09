package com.eventhub.aspect;

import com.eventhub.security.CustomUserDetails;
import com.eventhub.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private static final String SYSTEM_USERNAME = "system";

    private final AuditLogService auditLogService;

    @Around("@annotation(logAction)")
    public Object logAction(ProceedingJoinPoint joinPoint, LogAction logAction) throws Throwable {
        long startTime = System.currentTimeMillis();
        String username = getCurrentUsername();
        Long entityId = extractEntityIdFromArgs(joinPoint.getArgs(), logAction.entityIdArgIndex());

        try {
            Object result = joinPoint.proceed();

            if (entityId == null && logAction.useReturnedId()) {
                entityId = extractEntityIdFromResult(result);
            }

            long executionTimeMs = System.currentTimeMillis() - startTime;

            saveAuditLogSafely(
                    username,
                    logAction.action(),
                    logAction.entityType(),
                    entityId,
                    true,
                    null,
                    executionTimeMs);

            log.info(
                    "Audit action completed: action={}, entityType={}, entityId={}, username={}, executionTimeMs={}",
                    logAction.action(),
                    logAction.entityType(),
                    entityId,
                    username,
                    executionTimeMs);

            return result;
        } catch (Throwable exception) {
            long executionTimeMs = System.currentTimeMillis() - startTime;

            saveAuditLogSafely(
                    username,
                    logAction.action(),
                    logAction.entityType(),
                    entityId,
                    false,
                    exception.getMessage(),
                    executionTimeMs);

            log.warn(
                    "Audit action failed: action={}, entityType={}, entityId={}, username={}, executionTimeMs={}, error={}",
                    logAction.action(),
                    logAction.entityType(),
                    entityId,
                    username,
                    executionTimeMs,
                    exception.getMessage());

            throw exception;
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return SYSTEM_USERNAME;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUsername();
        }

        if (principal instanceof String username) {
            return username;
        }

        return SYSTEM_USERNAME;
    }

    private Long extractEntityIdFromArgs(Object[] args, int entityIdArgIndex) {
        if (entityIdArgIndex < 0 || entityIdArgIndex >= args.length) {
            return null;
        }

        Object argument = args[entityIdArgIndex];

        if (argument instanceof Long value) {
            return value;
        }

        if (argument instanceof Number number) {
            return number.longValue();
        }

        return null;
    }

    private Long extractEntityIdFromResult(Object result) {
        if (result == null) {
            return null;
        }

        try {
            Method getIdMethod = result.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(result);

            if (id instanceof Long value) {
                return value;
            }

            if (id instanceof Number number) {
                return number.longValue();
            }

            return null;
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    private void saveAuditLogSafely(
            String username,
            String action,
            String entityType,
            Long entityId,
            boolean success,
            String errorMessage,
            Long executionTimeMs) {
        try {
            auditLogService.saveLog(
                    username,
                    action,
                    entityType,
                    entityId,
                    success,
                    errorMessage,
                    executionTimeMs);
        } catch (RuntimeException exception) {
            log.warn("Failed to save audit log for action={}", action, exception);
        }
    }
}