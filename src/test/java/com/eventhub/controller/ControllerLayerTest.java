package com.eventhub.controller;

import com.eventhub.dto.request.CategoryCreateRequest;
import com.eventhub.dto.request.CategoryUpdateRequest;
import com.eventhub.dto.request.EventCreateRequest;
import com.eventhub.dto.request.EventUpdateRequest;
import com.eventhub.dto.request.LocationCreateRequest;
import com.eventhub.dto.request.LocationUpdateRequest;
import com.eventhub.dto.request.LoginRequest;
import com.eventhub.dto.request.OrganizerApplicationCreateRequest;
import com.eventhub.dto.request.OrganizerApplicationReviewRequest;
import com.eventhub.dto.request.RegisterRequest;
import com.eventhub.dto.request.RegistrationCreateRequest;
import com.eventhub.dto.request.ReviewCreateRequest;
import com.eventhub.dto.request.ReviewUpdateRequest;
import com.eventhub.dto.request.TicketTypeCreateRequest;
import com.eventhub.dto.request.TicketTypeUpdateRequest;
import com.eventhub.dto.request.UserUpdateRequest;
import com.eventhub.dto.response.AdminStatisticsResponse;
import com.eventhub.dto.response.AuditLogResponse;
import com.eventhub.dto.response.AuthResponse;
import com.eventhub.dto.response.CategoryResponse;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.dto.response.EventShortResponse;
import com.eventhub.dto.response.LocationResponse;
import com.eventhub.dto.response.OrganizerApplicationResponse;
import com.eventhub.dto.response.OrganizerStatisticsResponse;
import com.eventhub.dto.response.PaymentResponse;
import com.eventhub.dto.response.RegistrationResponse;
import com.eventhub.dto.response.ReviewResponse;
import com.eventhub.dto.response.TicketTypeResponse;
import com.eventhub.dto.response.UserResponse;
import com.eventhub.enums.EventStatus;
import com.eventhub.enums.OrganizerApplicationStatus;
import com.eventhub.enums.PaymentStatus;
import com.eventhub.enums.RegistrationStatus;
import com.eventhub.enums.UserStatus;
import com.eventhub.service.AuditLogService;
import com.eventhub.service.AuthService;
import com.eventhub.service.CategoryService;
import com.eventhub.service.EventService;
import com.eventhub.service.LocationService;
import com.eventhub.service.OrganizerApplicationService;
import com.eventhub.service.PaymentService;
import com.eventhub.service.RegistrationService;
import com.eventhub.service.ReviewService;
import com.eventhub.service.StatisticsService;
import com.eventhub.service.TicketTypeService;
import com.eventhub.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControllerLayerTest {

    @Mock
    private AuthService authService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private LocationService locationService;

    @Mock
    private EventService eventService;

    @Mock
    private TicketTypeService ticketTypeService;

    @Mock
    private RegistrationService registrationService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private OrganizerApplicationService organizerApplicationService;

    @Mock
    private UserService userService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuthController authController;

    @InjectMocks
    private CategoryController categoryController;

    @InjectMocks
    private LocationController locationController;

    @InjectMocks
    private EventController eventController;

    @InjectMocks
    private TicketTypeController ticketTypeController;

    @InjectMocks
    private RegistrationController registrationController;

    @InjectMocks
    private ReviewController reviewController;

    @InjectMocks
    private OrganizerApplicationController organizerApplicationController;

    @InjectMocks
    private AdminOrganizerApplicationController adminOrganizerApplicationController;

    @InjectMocks
    private UserController userController;

    @InjectMocks
    private AdminUserController adminUserController;

    @InjectMocks
    private PaymentController paymentController;

    @InjectMocks
    private StatisticsController statisticsController;

    @InjectMocks
    private AuditLogController auditLogController;

    @Test
    void registerShouldReturnCreatedResponse() {
        RegisterRequest request = RegisterRequest.builder()
                .username("polina")
                .email("polina@mail.ru")
                .password("123456")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("token")
                .user(userResponse())
                .build();

        when(authService.register(request)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.register(request);

        assertEquals(201, response.getStatusCode().value());
        assertSame(authResponse, response.getBody());
    }

    @Test
    void loginShouldReturnOkResponse() {
        LoginRequest request = LoginRequest.builder()
                .email("polina@mail.ru")
                .password("123456")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("token")
                .user(userResponse())
                .build();

        when(authService.login(request)).thenReturn(authResponse);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertSame(authResponse, response.getBody());
    }

    @Test
    void categoryControllerShouldReturnCategoriesAndCreateUpdateDeleteCategory() {
        PageRequest pageable = PageRequest.of(0, 10);
        CategoryResponse categoryResponse = categoryResponse();

        CategoryCreateRequest createRequest = CategoryCreateRequest.builder()
                .name("IT")
                .build();

        CategoryUpdateRequest updateRequest = CategoryUpdateRequest.builder()
                .name("Education")
                .build();

        when(categoryService.getAllCategories(pageable))
                .thenReturn(new PageImpl<>(List.of(categoryResponse)));
        when(categoryService.getCategoryById(1L)).thenReturn(categoryResponse);
        when(categoryService.createCategory(createRequest)).thenReturn(categoryResponse);
        when(categoryService.updateCategory(1L, updateRequest)).thenReturn(categoryResponse);

        assertEquals(1, Objects.requireNonNull(categoryController.getAllCategories(pageable).getBody()).getTotalElements());
        assertSame(categoryResponse, categoryController.getCategoryById(1L).getBody());
        assertEquals(201, categoryController.createCategory(createRequest).getStatusCode().value());
        assertSame(categoryResponse, categoryController.updateCategory(1L, updateRequest).getBody());
        assertEquals(204, categoryController.deleteCategory(1L).getStatusCode().value());

        verify(categoryService).deleteCategory(1L);
    }

    @Test
    void locationControllerShouldReturnLocationsAndCreateUpdateDeleteLocation() {
        PageRequest pageable = PageRequest.of(0, 10);
        LocationResponse locationResponse = locationResponse();

        LocationCreateRequest createRequest = LocationCreateRequest.builder()
                .country("Belarus")
                .city("Minsk")
                .address("Main street 1")
                .placeName("Conference Hall")
                .build();

        LocationUpdateRequest updateRequest = LocationUpdateRequest.builder()
                .country("Poland")
                .city("Warsaw")
                .address("Center 10")
                .placeName("Expo Center")
                .build();

        when(locationService.getAllLocations(pageable))
                .thenReturn(new PageImpl<>(List.of(locationResponse)));
        when(locationService.getLocationById(1L)).thenReturn(locationResponse);
        when(locationService.createLocation(createRequest)).thenReturn(locationResponse);
        when(locationService.updateLocation(1L, updateRequest)).thenReturn(locationResponse);

        assertEquals(1, Objects.requireNonNull(locationController.getAllLocations(pageable).getBody()).getTotalElements());
        assertSame(locationResponse, locationController.getLocationById(1L).getBody());
        assertEquals(201, locationController.createLocation(createRequest).getStatusCode().value());
        assertSame(locationResponse, locationController.updateLocation(1L, updateRequest).getBody());
        assertEquals(204, locationController.deleteLocation(1L).getStatusCode().value());

        verify(locationService).deleteLocation(1L);
    }

    @Test
    void eventControllerShouldReturnEventsAndManageEventLifecycle() {
        PageRequest pageable = PageRequest.of(0, 10);
        EventShortResponse shortResponse = EventShortResponse.builder()
                .id(1L)
                .title("Java Conference")
                .status(EventStatus.PUBLISHED)
                .build();

        EventResponse eventResponse = eventResponse();

        EventCreateRequest createRequest = EventCreateRequest.builder()
                .title("Java Conference")
                .description("Description")
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(10).plusHours(2))
                .capacity(100)
                .categoryId(1L)
                .locationId(1L)
                .build();

        EventUpdateRequest updateRequest = EventUpdateRequest.builder()
                .title("Updated Java Conference")
                .description("Updated description")
                .startDate(LocalDateTime.now().plusDays(12))
                .endDate(LocalDateTime.now().plusDays(12).plusHours(2))
                .capacity(120)
                .categoryId(1L)
                .locationId(1L)
                .build();

        when(eventService.getAllEvents(1L, "Minsk", "java", pageable))
                .thenReturn(new PageImpl<>(List.of(shortResponse)));
        when(eventService.getEventById(1L)).thenReturn(eventResponse);
        when(eventService.createEvent(createRequest)).thenReturn(eventResponse);
        when(eventService.updateEvent(1L, updateRequest)).thenReturn(eventResponse);
        when(eventService.publishEvent(1L)).thenReturn(eventResponse);
        when(eventService.cancelEvent(1L)).thenReturn(eventResponse);

        assertEquals(1, Objects.requireNonNull(eventController.getAllEvents(1L, "Minsk", "java", pageable)
                .getBody()).getTotalElements());
        assertSame(eventResponse, eventController.getEventById(1L).getBody());
        assertEquals(201, eventController.createEvent(createRequest).getStatusCode().value());
        assertSame(eventResponse, eventController.updateEvent(1L, updateRequest).getBody());
        assertSame(eventResponse, eventController.publishEvent(1L).getBody());
        assertSame(eventResponse, eventController.cancelEvent(1L).getBody());
        assertEquals(204, eventController.deleteEvent(1L).getStatusCode().value());

        verify(eventService).deleteEvent(1L);
    }

    @Test
    void ticketTypeControllerShouldReturnTicketTypesAndCreateUpdateDeleteTicketType() {
        TicketTypeResponse ticketTypeResponse = ticketTypeResponse();

        TicketTypeCreateRequest createRequest = TicketTypeCreateRequest.builder()
                .name("Standard")
                .price(BigDecimal.valueOf(50))
                .totalQuantity(20)
                .build();

        TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder()
                .name("VIP")
                .price(BigDecimal.valueOf(100))
                .totalQuantity(10)
                .build();

        when(ticketTypeService.createTicketType(1L, createRequest)).thenReturn(ticketTypeResponse);
        when(ticketTypeService.getTicketTypesByEvent(1L)).thenReturn(List.of(ticketTypeResponse));
        when(ticketTypeService.getTicketTypeById(2L)).thenReturn(ticketTypeResponse);
        when(ticketTypeService.updateTicketType(2L, updateRequest)).thenReturn(ticketTypeResponse);

        assertEquals(201, ticketTypeController.createTicketType(1L, createRequest).getStatusCode().value());
        assertEquals(1, Objects.requireNonNull(ticketTypeController.getTicketTypesByEvent(1L).getBody()).size());
        assertSame(ticketTypeResponse, ticketTypeController.getTicketTypeById(2L).getBody());
        assertSame(ticketTypeResponse, ticketTypeController.updateTicketType(2L, updateRequest).getBody());
        assertEquals(204, ticketTypeController.deleteTicketType(2L).getStatusCode().value());

        verify(ticketTypeService).deleteTicketType(2L);
    }

    @Test
    void registrationControllerShouldBuyCancelAndReturnRegistrations() {
        RegistrationCreateRequest request = RegistrationCreateRequest.builder()
                .ticketTypeId(1L)
                .build();

        RegistrationResponse registrationResponse = registrationResponse();

        when(registrationService.buyTicket(request)).thenReturn(registrationResponse);
        when(registrationService.cancelRegistration(1L)).thenReturn(registrationResponse);
        when(registrationService.getMyRegistrations()).thenReturn(List.of(registrationResponse));
        when(registrationService.getRegistrationsByEvent(10L)).thenReturn(List.of(registrationResponse));

        assertEquals(201, registrationController.buyTicket(request).getStatusCode().value());
        assertSame(registrationResponse, registrationController.cancelRegistration(1L).getBody());
        assertEquals(1, Objects.requireNonNull(registrationController.getMyRegistrations().getBody()).size());
        assertEquals(1, Objects.requireNonNull(registrationController.getRegistrationsByEvent(10L).getBody()).size());
    }

    @Test
    void reviewControllerShouldCreateGetUpdateAndDeleteReviews() {
        PageRequest pageable = PageRequest.of(0, 10);
        ReviewResponse reviewResponse = reviewResponse();

        ReviewCreateRequest createRequest = ReviewCreateRequest.builder()
                .rating(5)
                .comment("Great event")
                .build();

        ReviewUpdateRequest updateRequest = ReviewUpdateRequest.builder()
                .rating(4)
                .comment("Good event")
                .build();

        when(reviewService.createReview(1L, createRequest)).thenReturn(reviewResponse);
        when(reviewService.getReviewsByEvent(1L, pageable)).thenReturn(new PageImpl<>(List.of(reviewResponse)));
        when(reviewService.updateReview(2L, updateRequest)).thenReturn(reviewResponse);

        assertEquals(201, reviewController.createReview(1L, createRequest).getStatusCode().value());
        assertEquals(1, Objects.requireNonNull(reviewController.getReviewsByEvent(1L, pageable).getBody()).getTotalElements());
        assertSame(reviewResponse, reviewController.updateReview(2L, updateRequest).getBody());
        assertEquals(204, reviewController.deleteReview(2L).getStatusCode().value());

        verify(reviewService).deleteReview(2L);
    }

    @Test
    void organizerApplicationControllersShouldCreateReturnApproveAndRejectApplications() {
        PageRequest pageable = PageRequest.of(0, 10);
        OrganizerApplicationResponse applicationResponse = organizerApplicationResponse();

        OrganizerApplicationCreateRequest createRequest = OrganizerApplicationCreateRequest.builder()
                .organizationName("Event Company")
                .contactEmail("company@mail.com")
                .description("We organize IT events")
                .build();

        OrganizerApplicationReviewRequest reviewRequest = OrganizerApplicationReviewRequest.builder()
                .adminComment("Approved")
                .build();

        when(organizerApplicationService.createApplication(createRequest)).thenReturn(applicationResponse);
        when(organizerApplicationService.getMyApplications(pageable))
                .thenReturn(new PageImpl<>(List.of(applicationResponse)));
        when(organizerApplicationService.getApplications(OrganizerApplicationStatus.PENDING, pageable))
                .thenReturn(new PageImpl<>(List.of(applicationResponse)));
        when(organizerApplicationService.approveApplication(1L, reviewRequest)).thenReturn(applicationResponse);
        when(organizerApplicationService.rejectApplication(1L, reviewRequest)).thenReturn(applicationResponse);

        assertEquals(201, organizerApplicationController.createApplication(createRequest).getStatusCode().value());
        assertEquals(1, Objects.requireNonNull(organizerApplicationController.getMyApplications(pageable).getBody()).getTotalElements());
        assertEquals(1, Objects.requireNonNull(adminOrganizerApplicationController
                        .getApplications(OrganizerApplicationStatus.PENDING, pageable)
                        .getBody())
                .getTotalElements());
        assertSame(applicationResponse, adminOrganizerApplicationController.approveApplication(1L, reviewRequest).getBody());
        assertSame(applicationResponse, adminOrganizerApplicationController.rejectApplication(1L, reviewRequest).getBody());
    }

    @Test
    void userControllersShouldReturnAndManageUsers() {
        PageRequest pageable = PageRequest.of(0, 10);
        UserResponse userResponse = userResponse();

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .username("updated")
                .email("updated@mail.ru")
                .build();

        when(userService.getCurrentUser()).thenReturn(userResponse);
        when(userService.updateCurrentUser(updateRequest)).thenReturn(userResponse);
        when(userService.getAllUsers(pageable)).thenReturn(new PageImpl<>(List.of(userResponse)));
        when(userService.getUserById(1L)).thenReturn(userResponse);
        when(userService.blockUser(1L)).thenReturn(userResponse);
        when(userService.unblockUser(1L)).thenReturn(userResponse);

        assertSame(userResponse, userController.getCurrentUser().getBody());
        assertSame(userResponse, userController.updateCurrentUser(updateRequest).getBody());
        assertEquals(1, Objects.requireNonNull(adminUserController.getAllUsers(pageable).getBody()).getTotalElements());
        assertSame(userResponse, adminUserController.getUserById(1L).getBody());
        assertSame(userResponse, adminUserController.blockUser(1L).getBody());
        assertSame(userResponse, adminUserController.unblockUser(1L).getBody());
    }

    @Test
    void paymentControllerShouldReturnPayments() {
        PageRequest pageable = PageRequest.of(0, 10);
        PaymentResponse paymentResponse = paymentResponse();

        when(paymentService.getAllPayments(pageable)).thenReturn(new PageImpl<>(List.of(paymentResponse)));
        when(paymentService.getPaymentById(1L)).thenReturn(paymentResponse);
        when(paymentService.getOrganizerPayments(pageable)).thenReturn(new PageImpl<>(List.of(paymentResponse)));

        assertEquals(1, Objects.requireNonNull(paymentController.getAllPayments(pageable).getBody()).getTotalElements());
        assertSame(paymentResponse, paymentController.getPaymentById(1L).getBody());
        assertEquals(1, Objects.requireNonNull(paymentController.getOrganizerPayments(pageable).getBody()).getTotalElements());
    }

    @Test
    void statisticsControllerShouldReturnOrganizerAndAdminStatistics() {
        OrganizerStatisticsResponse organizerStatistics = OrganizerStatisticsResponse.builder()
                .organizerId(1L)
                .eventsCount(2L)
                .soldTickets(10L)
                .totalRevenue(BigDecimal.valueOf(500))
                .averageRating(BigDecimal.valueOf(4.50))
                .mostPopularEventTitle("Java Conference")
                .mostPopularEventTicketsSold(7L)
                .build();

        AdminStatisticsResponse adminStatistics = AdminStatisticsResponse.builder()
                .usersCount(10L)
                .organizersCount(2L)
                .eventsCount(5L)
                .registrationsCount(20L)
                .totalRevenue(BigDecimal.valueOf(1000))
                .blockedUsersCount(1L)
                .build();

        when(statisticsService.getOrganizerStatistics()).thenReturn(organizerStatistics);
        when(statisticsService.getAdminStatistics()).thenReturn(adminStatistics);

        assertSame(organizerStatistics, statisticsController.getOrganizerStatistics().getBody());
        assertSame(adminStatistics, statisticsController.getAdminStatistics().getBody());
    }

    @Test
    void auditLogControllerShouldReturnAuditLogs() {
        PageRequest pageable = PageRequest.of(0, 10);
        AuditLogResponse auditLogResponse = AuditLogResponse.builder()
                .id(1L)
                .username("admin@eventhub.com")
                .action("BLOCK_USER")
                .entityType("USER")
                .entityId(2L)
                .success(true)
                .executionTimeMs(25L)
                .build();

        when(auditLogService.getAuditLogs(pageable)).thenReturn(new PageImpl<>(List.of(auditLogResponse)));

        assertEquals(1, Objects.requireNonNull(auditLogController.getAuditLogs(pageable).getBody()).getTotalElements());
    }

    private UserResponse userResponse() {
        return UserResponse.builder()
                .id(1L)
                .username("polina")
                .email("polina@mail.ru")
                .status(UserStatus.ACTIVE)
                .roles(Set.of("ROLE_USER"))
                .build();
    }

    private CategoryResponse categoryResponse() {
        return CategoryResponse.builder()
                .id(1L)
                .name("IT")
                .build();
    }

    private LocationResponse locationResponse() {
        return LocationResponse.builder()
                .id(1L)
                .country("Belarus")
                .city("Minsk")
                .address("Main street 1")
                .placeName("Conference Hall")
                .build();
    }

    private EventResponse eventResponse() {
        return EventResponse.builder()
                .id(1L)
                .title("Java Conference")
                .description("Description")
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(10).plusHours(2))
                .capacity(100)
                .status(EventStatus.PUBLISHED)
                .rating(BigDecimal.valueOf(4.50))
                .organizerId(1L)
                .organizerUsername("organizer")
                .organizerEmail("organizer@mail.ru")
                .categoryId(1L)
                .categoryName("IT")
                .locationId(1L)
                .country("Belarus")
                .city("Minsk")
                .address("Main street 1")
                .placeName("Conference Hall")
                .build();
    }

    private TicketTypeResponse ticketTypeResponse() {
        return TicketTypeResponse.builder()
                .id(1L)
                .eventId(1L)
                .eventTitle("Java Conference")
                .name("Standard")
                .price(BigDecimal.valueOf(50))
                .totalQuantity(20)
                .availableQuantity(20)
                .build();
    }

    private RegistrationResponse registrationResponse() {
        return RegistrationResponse.builder()
                .id(1L)
                .userId(1L)
                .username("polina")
                .userEmail("polina@mail.ru")
                .eventId(1L)
                .eventTitle("Java Conference")
                .ticketTypeId(1L)
                .ticketTypeName("Standard")
                .price(BigDecimal.valueOf(50))
                .status(RegistrationStatus.ACTIVE)
                .paymentId(1L)
                .paymentAmount(BigDecimal.valueOf(50))
                .paymentStatus(PaymentStatus.PAID)
                .build();
    }

    private ReviewResponse reviewResponse() {
        return ReviewResponse.builder()
                .id(1L)
                .eventId(1L)
                .eventTitle("Java Conference")
                .userId(1L)
                .username("polina")
                .rating(5)
                .comment("Great event")
                .build();
    }

    private OrganizerApplicationResponse organizerApplicationResponse() {
        return OrganizerApplicationResponse.builder()
                .id(1L)
                .userId(1L)
                .username("polina")
                .userEmail("polina@mail.com")
                .organizationName("Event Company")
                .contactEmail("company@mail.ru")
                .description("We organize IT events")
                .status(OrganizerApplicationStatus.PENDING)
                .build();
    }

    private PaymentResponse paymentResponse() {
        return PaymentResponse.builder()
                .id(1L)
                .registrationId(1L)
                .userId(1L)
                .username("polina")
                .userEmail("polina@mail.ru")
                .eventId(1L)
                .eventTitle("Java Conference")
                .ticketTypeId(1L)
                .ticketTypeName("Standard")
                .amount(BigDecimal.valueOf(50))
                .status(PaymentStatus.PAID)
                .build();
    }
}