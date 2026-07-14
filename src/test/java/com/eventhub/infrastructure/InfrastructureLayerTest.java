package com.eventhub.infrastructure;

import com.eventhub.aspect.LogAction;
import com.eventhub.aspect.LoggingAspect;
import com.eventhub.config.OpenApiConfig;
import com.eventhub.config.PasswordEncoderConfig;
import com.eventhub.entity.Event;
import com.eventhub.enums.EventStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.repository.EventRepository;
import com.eventhub.scheduler.EventStatusScheduler;
import com.eventhub.service.AuditLogService;
import com.eventhub.support.TestDataFactory;
import io.swagger.v3.oas.models.OpenAPI;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InfrastructureLayerTest {

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private LogAction logAction;

    @Test
    void passwordEncoderShouldEncodeAndMatchPassword() {
        PasswordEncoder passwordEncoder = new PasswordEncoderConfig().passwordEncoder();

        String encodedPassword = passwordEncoder.encode("123456");

        assertTrue(passwordEncoder.matches("123456", encodedPassword));
        assertFalse(passwordEncoder.matches("wrong-password", encodedPassword));
    }

    @Test
    void openApiConfigShouldCreateApiInfoAndJwtSecurityScheme() {
        OpenAPI openAPI = new OpenApiConfig().eventHubOpenApi();

        assertEquals("EventHub API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertTrue(openAPI.getInfo().getDescription().contains("event management"));
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("bearerAuth"));
        assertEquals("bearer", openAPI.getComponents().getSecuritySchemes().get("bearerAuth").getScheme());
        assertEquals("JWT", openAPI.getComponents().getSecuritySchemes().get("bearerAuth").getBearerFormat());
    }

    @Test
    void finishPastEventsShouldChangePublishedPastEventsToFinished() {
        Event event = TestDataFactory.finishedEvent(1L, TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER));
        event.setStatus(EventStatus.PUBLISHED);

        when(eventRepository.findByStatusAndEndDateBefore(eq(EventStatus.PUBLISHED), any(LocalDateTime.class))).thenReturn(List.of(event));

        EventStatusScheduler scheduler = new EventStatusScheduler(eventRepository);

        scheduler.finishPastEvents();

        assertEquals(EventStatus.FINISHED, event.getStatus());
        verify(eventRepository).findByStatusAndEndDateBefore(eq(EventStatus.PUBLISHED), any(LocalDateTime.class));
    }

    @Test
    void finishPastEventsShouldDoNothingWhenThereAreNoPastEvents() {
        when(eventRepository.findByStatusAndEndDateBefore(eq(EventStatus.PUBLISHED), any(LocalDateTime.class))).thenReturn(List.of());

        EventStatusScheduler scheduler = new EventStatusScheduler(eventRepository);

        scheduler.finishPastEvents();

        verify(eventRepository).findByStatusAndEndDateBefore(eq(EventStatus.PUBLISHED), any(LocalDateTime.class));
    }

    @Test
    void loggingAspectShouldSaveSuccessfulAuditLogUsingArgumentId() throws Throwable {
        LoggingAspect loggingAspect = new LoggingAspect(auditLogService);

        when(joinPoint.getArgs()).thenReturn(new Object[]{7L});
        when(joinPoint.proceed()).thenReturn("OK");

        when(logAction.entityIdArgIndex()).thenReturn(0);
        when(logAction.action()).thenReturn("CREATE_EVENT");
        when(logAction.entityType()).thenReturn("EVENT");

        Object result = loggingAspect.logAction(joinPoint, logAction);

        assertEquals("OK", result);

        verify(auditLogService).saveLog(
                eq("system"),
                eq("CREATE_EVENT"),
                eq("EVENT"),
                eq(7L),
                eq(true),
                isNull(),
                anyLong());
    }

    @Test
    void loggingAspectShouldSaveSuccessfulAuditLogUsingReturnedId() throws Throwable {
        LoggingAspect loggingAspect = new LoggingAspect(auditLogService);
        ResultWithId returnedObject = new ResultWithId(15L);

        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenReturn(returnedObject);

        when(logAction.entityIdArgIndex()).thenReturn(-1);
        when(logAction.useReturnedId()).thenReturn(true);
        when(logAction.action()).thenReturn("BUY_TICKET");
        when(logAction.entityType()).thenReturn("REGISTRATION");

        Object result = loggingAspect.logAction(joinPoint, logAction);

        assertSame(returnedObject, result);

        verify(auditLogService).saveLog(
                eq("system"),
                eq("BUY_TICKET"),
                eq("REGISTRATION"),
                eq(15L),
                eq(true),
                isNull(),
                anyLong());
    }

    @Test
    void loggingAspectShouldSaveFailedAuditLogAndRethrowException() throws Throwable {
        LoggingAspect loggingAspect = new LoggingAspect(auditLogService);
        IllegalStateException exception = new IllegalStateException("Something went wrong");

        when(joinPoint.getArgs()).thenReturn(new Object[]{9L});
        when(joinPoint.proceed()).thenThrow(exception);

        when(logAction.entityIdArgIndex()).thenReturn(0);
        when(logAction.action()).thenReturn("CANCEL_EVENT");
        when(logAction.entityType()).thenReturn("EVENT");

        IllegalStateException actualException = assertThrows(IllegalStateException.class, () -> loggingAspect.logAction(joinPoint, logAction));

        assertSame(exception, actualException);

        verify(auditLogService).saveLog(
                eq("system"),
                eq("CANCEL_EVENT"),
                eq("EVENT"),
                eq(9L),
                eq(false),
                eq("Something went wrong"),
                anyLong());
    }

    public static class ResultWithId {

        private final Long id;

        ResultWithId(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }
}