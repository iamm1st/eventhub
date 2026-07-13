package com.eventhub.mapper;

import com.eventhub.dto.response.AuditLogResponse;
import com.eventhub.dto.response.CategoryResponse;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.dto.response.EventShortResponse;
import com.eventhub.dto.response.LocationResponse;
import com.eventhub.dto.response.OrganizerApplicationResponse;
import com.eventhub.dto.response.PaymentResponse;
import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.dto.response.ReviewResponse;
import com.eventhub.dto.response.TicketTypeResponse;
import com.eventhub.dto.response.UserResponse;
import com.eventhub.entity.AuditLog;
import com.eventhub.entity.Event;
import com.eventhub.entity.EventCategory;
import com.eventhub.entity.Location;
import com.eventhub.entity.OrganizerApplication;
import com.eventhub.entity.Payment;
import com.eventhub.entity.Registration;
import com.eventhub.entity.Review;
import com.eventhub.entity.TicketType;
import com.eventhub.entity.User;
import com.eventhub.enums.OrganizerApplicationStatus;
import com.eventhub.enums.PaymentStatus;
import com.eventhub.enums.RegistrationStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapperLayerTest {

    private final AuditLogMapper auditLogMapper = new AuditLogMapper();
    private final CategoryMapper categoryMapper = new CategoryMapper();
    private final EventMapper eventMapper = new EventMapper();
    private final LocationMapper locationMapper = new LocationMapper();
    private final OrganizerApplicationMapper organizerApplicationMapper = new OrganizerApplicationMapper();
    private final PaymentMapper paymentMapper = new PaymentMapper();
    private final RegistrationMapper registrationMapper = new RegistrationMapper();
    private final ReviewMapper reviewMapper = new ReviewMapper();
    private final TicketTypeMapper ticketTypeMapper = new TicketTypeMapper();
    private final UserMapper userMapper = new UserMapper();

    @Test
    void categoryMapperShouldMapEntityToResponse() {
        EventCategory category = TestDataFactory.category(1L);

        CategoryResponse response = categoryMapper.toResponse(category);

        assertEquals(category.getId(), response.getId());
        assertEquals(category.getName(), response.getName());
        assertEquals(category.getCreatedAt(), response.getCreatedAt());
        assertEquals(category.getUpdatedAt(), response.getUpdatedAt());
    }

    @Test
    void locationMapperShouldMapEntityToResponse() {
        Location location = TestDataFactory.location(1L);

        LocationResponse response = locationMapper.toResponse(location);

        assertEquals(location.getId(), response.getId());
        assertEquals(location.getCountry(), response.getCountry());
        assertEquals(location.getCity(), response.getCity());
        assertEquals(location.getAddress(), response.getAddress());
        assertEquals(location.getPlaceName(), response.getPlaceName());
        assertEquals(location.getCreatedAt(), response.getCreatedAt());
        assertEquals(location.getUpdatedAt(), response.getUpdatedAt());
    }

    @Test
    void userMapperShouldMapEntityToResponse() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER, RoleName.ROLE_ORGANIZER);

        UserResponse response = userMapper.toResponse(user);

        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getStatus(), response.getStatus());
        assertEquals(user.getCreatedAt(), response.getCreatedAt());
        assertEquals(user.getUpdatedAt(), response.getUpdatedAt());
        assertTrue(response.getRoles().contains("ROLE_USER"));
        assertTrue(response.getRoles().contains("ROLE_ORGANIZER"));
    }

    @Test
    void eventMapperShouldMapEntityToFullResponse() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);

        EventResponse response = eventMapper.toResponse(event);

        assertEquals(event.getId(), response.getId());
        assertEquals(event.getTitle(), response.getTitle());
        assertEquals(event.getDescription(), response.getDescription());
        assertEquals(event.getStartDate(), response.getStartDate());
        assertEquals(event.getEndDate(), response.getEndDate());
        assertEquals(event.getCapacity(), response.getCapacity());
        assertEquals(event.getStatus(), response.getStatus());
        assertEquals(event.getRating(), response.getRating());

        assertEquals(event.getOrganizer().getId(), response.getOrganizerId());
        assertEquals(event.getOrganizer().getUsername(), response.getOrganizerUsername());
        assertEquals(event.getOrganizer().getEmail(), response.getOrganizerEmail());

        assertEquals(event.getCategory().getId(), response.getCategoryId());
        assertEquals(event.getCategory().getName(), response.getCategoryName());

        assertEquals(event.getLocation().getId(), response.getLocationId());
        assertEquals(event.getLocation().getCountry(), response.getCountry());
        assertEquals(event.getLocation().getCity(), response.getCity());
        assertEquals(event.getLocation().getAddress(), response.getAddress());
        assertEquals(event.getLocation().getPlaceName(), response.getPlaceName());
    }

    @Test
    void eventMapperShouldMapEntityToShortResponse() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);

        EventShortResponse response = eventMapper.toShortResponse(event);

        assertEquals(event.getId(), response.getId());
        assertEquals(event.getTitle(), response.getTitle());
        assertEquals(event.getStartDate(), response.getStartDate());
        assertEquals(event.getEndDate(), response.getEndDate());
        assertEquals(event.getCapacity(), response.getCapacity());
        assertEquals(event.getStatus(), response.getStatus());
        assertEquals(event.getRating(), response.getRating());
        assertEquals(event.getCategory().getName(), response.getCategoryName());
        assertEquals(event.getLocation().getCity(), response.getCity());
        assertEquals(event.getLocation().getPlaceName(), response.getPlaceName());
    }

    @Test
    void ticketTypeMapperShouldMapEntityToResponse() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(100L, event, 5);

        TicketTypeResponse response = ticketTypeMapper.toResponse(ticketType);

        assertEquals(ticketType.getId(), response.getId());
        assertEquals(ticketType.getEvent().getId(), response.getEventId());
        assertEquals(ticketType.getEvent().getTitle(), response.getEventTitle());
        assertEquals(ticketType.getName(), response.getName());
        assertEquals(ticketType.getPrice(), response.getPrice());
        assertEquals(ticketType.getTotalQuantity(), response.getTotalQuantity());
        assertEquals(ticketType.getAvailableQuantity(), response.getAvailableQuantity());
        assertEquals(ticketType.getCreatedAt(), response.getCreatedAt());
        assertEquals(ticketType.getUpdatedAt(), response.getUpdatedAt());
    }

    @Test
    void registrationMapperShouldMapEntityAndPaymentToResponse() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(20L, event, 5);
        Registration registration = TestDataFactory.activeRegistration(30L, user, event, ticketType);
        Payment payment = TestDataFactory.paidPayment(40L, registration);

        RegistrationResponse response = registrationMapper.toResponse(registration, payment);

        assertEquals(registration.getId(), response.getId());

        assertEquals(user.getId(), response.getUserId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getUserEmail());

        assertEquals(event.getId(), response.getEventId());
        assertEquals(event.getTitle(), response.getEventTitle());

        assertEquals(ticketType.getId(), response.getTicketTypeId());
        assertEquals(ticketType.getName(), response.getTicketTypeName());
        assertEquals(ticketType.getPrice(), response.getPrice());

        assertEquals(RegistrationStatus.ACTIVE, response.getStatus());

        assertEquals(payment.getId(), response.getPaymentId());
        assertEquals(payment.getAmount(), response.getPaymentAmount());
        assertEquals(PaymentStatus.PAID, response.getPaymentStatus());
    }

    @Test
    void paymentMapperShouldMapEntityToResponse() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        TicketType ticketType = TestDataFactory.ticketType(20L, event, 5);
        Registration registration = TestDataFactory.activeRegistration(30L, user, event, ticketType);
        Payment payment = TestDataFactory.paidPayment(40L, registration);

        PaymentResponse response = paymentMapper.toResponse(payment);

        assertEquals(payment.getId(), response.getId());
        assertEquals(registration.getId(), response.getRegistrationId());

        assertEquals(user.getId(), response.getUserId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getUserEmail());

        assertEquals(event.getId(), response.getEventId());
        assertEquals(event.getTitle(), response.getEventTitle());

        assertEquals(ticketType.getId(), response.getTicketTypeId());
        assertEquals(ticketType.getName(), response.getTicketTypeName());

        assertEquals(payment.getAmount(), response.getAmount());
        assertEquals(payment.getStatus(), response.getStatus());
    }

    @Test
    void reviewMapperShouldMapEntityToResponse() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);
        User organizer = TestDataFactory.user(2L, RoleName.ROLE_ORGANIZER);
        Event event = TestDataFactory.finishedEvent(10L, organizer);
        Review review = TestDataFactory.review(100L, user, event);

        ReviewResponse response = reviewMapper.toResponse(review);

        assertEquals(review.getId(), response.getId());
        assertEquals(event.getId(), response.getEventId());
        assertEquals(event.getTitle(), response.getEventTitle());
        assertEquals(user.getId(), response.getUserId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(review.getRating(), response.getRating());
        assertEquals(review.getComment(), response.getComment());
    }

    @Test
    void organizerApplicationMapperShouldMapEntityToResponse() {
        User user = TestDataFactory.user(1L, RoleName.ROLE_USER);

        OrganizerApplication application = OrganizerApplication.builder()
                .id(10L)
                .user(user)
                .organizationName("Event Company")
                .contactEmail("company@mail.com")
                .contactPhone("+375291111111")
                .description("We organize IT events")
                .websiteUrl("https://event-company.com")
                .status(OrganizerApplicationStatus.PENDING)
                .adminComment(null)
                .build();

        OrganizerApplicationResponse response = organizerApplicationMapper.toResponse(application);

        assertEquals(application.getId(), response.getId());

        assertEquals(user.getId(), response.getUserId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getUserEmail());

        assertEquals(application.getOrganizationName(), response.getOrganizationName());
        assertEquals(application.getContactEmail(), response.getContactEmail());
        assertEquals(application.getContactPhone(), response.getContactPhone());
        assertEquals(application.getDescription(), response.getDescription());
        assertEquals(application.getWebsiteUrl(), response.getWebsiteUrl());
        assertEquals(application.getStatus(), response.getStatus());
        assertEquals(application.getAdminComment(), response.getAdminComment());
    }

    @Test
    void auditLogMapperShouldMapEntityToResponse() {
        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .username("admin@eventhub.com")
                .action("BLOCK_USER")
                .entityType("USER")
                .entityId(2L)
                .success(true)
                .errorMessage(null)
                .executionTimeMs(25L)
                .build();

        AuditLogResponse response = auditLogMapper.toResponse(auditLog);

        assertEquals(auditLog.getId(), response.getId());
        assertEquals(auditLog.getUsername(), response.getUsername());
        assertEquals(auditLog.getAction(), response.getAction());
        assertEquals(auditLog.getEntityType(), response.getEntityType());
        assertEquals(auditLog.getEntityId(), response.getEntityId());
        assertTrue(response.isSuccess());
        assertEquals(auditLog.getErrorMessage(), response.getErrorMessage());
        assertEquals(auditLog.getExecutionTimeMs(), response.getExecutionTimeMs());
    }
}