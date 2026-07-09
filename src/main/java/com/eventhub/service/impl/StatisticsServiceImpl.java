package com.eventhub.service.impl;

import com.eventhub.dto.response.AdminStatisticsResponse;
import com.eventhub.dto.response.OrganizerStatisticsResponse;
import com.eventhub.entity.Event;
import com.eventhub.entity.Registration;
import com.eventhub.enums.PaymentStatus;
import com.eventhub.enums.RegistrationStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.enums.UserStatus;
import com.eventhub.repository.EventRepository;
import com.eventhub.repository.PaymentRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private static final int RATING_SCALE = 2;

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public OrganizerStatisticsResponse getOrganizerStatistics() {
        Long organizerId = currentUserProvider.getCurrentUserId();

        Long eventsCount = eventRepository.countByOrganizerId(organizerId);
        Long soldTickets = registrationRepository.countByEventOrganizerIdAndStatus(organizerId, RegistrationStatus.ACTIVE);
        BigDecimal totalRevenue = paymentRepository.sumAmountByStatusAndOrganizerId(PaymentStatus.PAID, organizerId);

        List<Event> organizerEvents = eventRepository.findByOrganizerId(organizerId);
        BigDecimal averageRating = calculateAverageRating(organizerEvents);

        List<Registration> activeRegistrations = registrationRepository.findByEventOrganizerIdAndStatus(organizerId, RegistrationStatus.ACTIVE);

        Optional<Map.Entry<Event, Long>> mostPopularEvent = activeRegistrations.stream()
                .collect(Collectors.groupingBy(Registration::getEvent, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue());

        return OrganizerStatisticsResponse.builder()
                .organizerId(organizerId)
                .eventsCount(eventsCount)
                .soldTickets(soldTickets)
                .totalRevenue(totalRevenue)
                .averageRating(averageRating)
                .mostPopularEventTitle(mostPopularEvent
                        .map(entry -> entry.getKey().getTitle())
                        .orElse(null))
                .mostPopularEventTicketsSold(mostPopularEvent
                        .map(Map.Entry::getValue)
                        .orElse(0L))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatisticsResponse getAdminStatistics() {
        return AdminStatisticsResponse.builder()
                .usersCount(userRepository.count())
                .organizersCount(userRepository.countByRoleName(RoleName.ROLE_ORGANIZER))
                .eventsCount(eventRepository.count())
                .registrationsCount(registrationRepository.count())
                .totalRevenue(paymentRepository.sumAmountByStatus(PaymentStatus.PAID))
                .blockedUsersCount(userRepository.countByStatus(UserStatus.BLOCKED))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private BigDecimal calculateAverageRating(List<Event> events) {
        return events.stream()
                .map(Event::getRating)
                .filter(rating -> rating != null && rating.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal::add)
                .map(sum -> divideByEventsCount(sum, events))
                .orElse(BigDecimal.ZERO.setScale(RATING_SCALE, RoundingMode.HALF_UP));
    }

    private BigDecimal divideByEventsCount(BigDecimal sum, List<Event> events) {
        long ratedEventsCount = events.stream()
                .map(Event::getRating)
                .filter(rating -> rating != null && rating.compareTo(BigDecimal.ZERO) > 0)
                .count();

        if (ratedEventsCount == 0) {
            return BigDecimal.ZERO.setScale(RATING_SCALE, RoundingMode.HALF_UP);
        }

        return sum.divide(BigDecimal.valueOf(ratedEventsCount), RATING_SCALE, RoundingMode.HALF_UP);
    }
}