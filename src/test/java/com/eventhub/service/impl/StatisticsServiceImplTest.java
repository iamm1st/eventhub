package com.eventhub.service.impl;

import com.eventhub.dto.response.AdminStatisticsResponse;
import com.eventhub.dto.response.OrganizerStatisticsResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.Registration;
import com.eventhub.entity.TicketType;
import com.eventhub.entity.User;
import com.eventhub.enums.PaymentStatus;
import com.eventhub.enums.RegistrationStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.enums.UserStatus;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.PaymentRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    @Test
    void getAdminStatisticsShouldReturnPlatformStatistics() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByRoleName(RoleName.ROLE_ORGANIZER)).thenReturn(3L);
        when(eventRepository.count()).thenReturn(7L);
        when(registrationRepository.count()).thenReturn(20L);
        when(paymentRepository.sumAmountByStatus(PaymentStatus.PAID)).thenReturn(BigDecimal.valueOf(1000));
        when(userRepository.countByStatus(UserStatus.BLOCKED)).thenReturn(2L);

        AdminStatisticsResponse response = statisticsService.getAdminStatistics();

        assertEquals(10L, response.getUsersCount());
        assertEquals(3L, response.getOrganizersCount());
        assertEquals(7L, response.getEventsCount());
        assertEquals(20L, response.getRegistrationsCount());
        assertEquals(BigDecimal.valueOf(1000), response.getTotalRevenue());
        assertEquals(2L, response.getBlockedUsersCount());
    }

    @Test
    void getOrganizerStatisticsShouldReturnOrganizerStatisticsWithMostPopularEvent() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);
        User user = TestDataFactory.user(2L, RoleName.ROLE_USER);

        Event firstEvent = TestDataFactory.futurePublishedEvent(10L, organizer);
        firstEvent.setTitle("Java Conference");
        firstEvent.setRating(BigDecimal.valueOf(5));

        Event secondEvent = TestDataFactory.futurePublishedEvent(11L, organizer);
        secondEvent.setTitle("Spring Workshop");
        secondEvent.setRating(BigDecimal.valueOf(4));

        TicketType firstTicketType = TestDataFactory.ticketType(20L, firstEvent, 10);
        TicketType secondTicketType = TestDataFactory.ticketType(21L, secondEvent, 10);

        Registration registration1 = TestDataFactory.activeRegistration(100L, user, firstEvent, firstTicketType);
        Registration registration2 = TestDataFactory.activeRegistration(101L, user, firstEvent, firstTicketType);
        Registration registration3 = TestDataFactory.activeRegistration(102L, user, secondEvent, secondTicketType);

        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());
        when(eventRepository.countByOrganizerId(organizer.getId())).thenReturn(2L);
        when(registrationRepository.countByEventOrganizerIdAndStatus(organizer.getId(), RegistrationStatus.ACTIVE)).thenReturn(3L);
        when(paymentRepository.sumAmountByStatusAndOrganizerId(PaymentStatus.PAID, organizer.getId())).thenReturn(BigDecimal.valueOf(150));
        when(eventRepository.findByOrganizerId(organizer.getId())).thenReturn(List.of(firstEvent, secondEvent));
        when(registrationRepository.findByEventOrganizerIdAndStatus(organizer.getId(), RegistrationStatus.ACTIVE))
                .thenReturn(List.of(registration1, registration2, registration3));

        OrganizerStatisticsResponse response = statisticsService.getOrganizerStatistics();

        assertEquals(organizer.getId(), response.getOrganizerId());
        assertEquals(2L, response.getEventsCount());
        assertEquals(3L, response.getSoldTickets());
        assertEquals(BigDecimal.valueOf(150), response.getTotalRevenue());
        assertEquals(BigDecimal.valueOf(4.50).setScale(2), response.getAverageRating());
        assertEquals("Java Conference", response.getMostPopularEventTitle());
        assertEquals(2L, response.getMostPopularEventTicketsSold());
    }

    @Test
    void getOrganizerStatisticsShouldReturnZeroRatingWhenThereAreNoRatedEvents() {
        User organizer = TestDataFactory.user(1L, RoleName.ROLE_ORGANIZER);

        Event event = TestDataFactory.futurePublishedEvent(10L, organizer);
        event.setRating(BigDecimal.ZERO);

        when(currentUserProvider.getCurrentUserId()).thenReturn(organizer.getId());
        when(eventRepository.countByOrganizerId(organizer.getId())).thenReturn(1L);
        when(registrationRepository.countByEventOrganizerIdAndStatus(organizer.getId(), RegistrationStatus.ACTIVE)).thenReturn(0L);
        when(paymentRepository.sumAmountByStatusAndOrganizerId(PaymentStatus.PAID, organizer.getId())).thenReturn(BigDecimal.ZERO);
        when(eventRepository.findByOrganizerId(organizer.getId())).thenReturn(List.of(event));
        when(registrationRepository.findByEventOrganizerIdAndStatus(organizer.getId(), RegistrationStatus.ACTIVE)).thenReturn(List.of());

        OrganizerStatisticsResponse response = statisticsService.getOrganizerStatistics();

        assertEquals(BigDecimal.ZERO.setScale(2), response.getAverageRating());
        assertEquals(0L, response.getMostPopularEventTicketsSold());
    }
}