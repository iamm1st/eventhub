package com.eventhub.support;

import com.eventhub.entity.Event;
import com.eventhub.entity.EventCategory;
import com.eventhub.entity.Location;
import com.eventhub.entity.Payment;
import com.eventhub.entity.Registration;
import com.eventhub.entity.Review;
import com.eventhub.entity.Role;
import com.eventhub.entity.TicketType;
import com.eventhub.entity.User;
import com.eventhub.enums.EventStatus;
import com.eventhub.enums.PaymentStatus;
import com.eventhub.enums.RegistrationStatus;
import com.eventhub.enums.RoleName;
import com.eventhub.enums.UserStatus;
import com.eventhub.security.CustomUserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Role role(RoleName roleName) {
        return Role.builder()
                .id((long) roleName.ordinal() + 1)
                .name(roleName)
                .build();
    }

    public static User user(Long id, RoleName... roleNames) {
        Set<Role> roles = new HashSet<>();

        for (RoleName roleName : roleNames) {
            roles.add(role(roleName));
        }

        return User.builder()
                .id(id)
                .username("user" + id)
                .email("user" + id + "@mail.com")
                .password("encoded-password")
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .build();
    }

    public static User blockedUser(Long id, RoleName... roleNames) {
        User user = user(id, roleNames);
        user.setStatus(UserStatus.BLOCKED);
        return user;
    }

    public static EventCategory category(Long id) {
        return EventCategory.builder()
                .id(id)
                .name("IT")
                .build();
    }

    public static Location location(Long id) {
        return Location.builder()
                .id(id)
                .country("Belarus")
                .city("Minsk")
                .address("Main street 1")
                .placeName("Conference Hall")
                .build();
    }

    public static Event futurePublishedEvent(Long id, User organizer) {
        return Event.builder()
                .id(id)
                .title("Java Conference")
                .description("Java event")
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(10).plusHours(2))
                .capacity(100)
                .status(EventStatus.PUBLISHED)
                .rating(BigDecimal.ZERO)
                .organizer(organizer)
                .category(category(1L))
                .location(location(1L))
                .build();
    }

    public static Event futureDraftEvent(Long id, User organizer) {
        Event event = futurePublishedEvent(id, organizer);
        event.setStatus(EventStatus.DRAFT);
        return event;
    }

    public static Event finishedEvent(Long id, User organizer) {
        Event event = futurePublishedEvent(id, organizer);
        event.setStatus(EventStatus.FINISHED);
        event.setStartDate(LocalDateTime.now().minusDays(2));
        event.setEndDate(LocalDateTime.now().minusDays(1));
        return event;
    }

    public static TicketType ticketType(Long id, Event event, int availableQuantity) {
        return TicketType.builder()
                .id(id)
                .event(event)
                .name("Standard")
                .price(BigDecimal.valueOf(50))
                .totalQuantity(10)
                .availableQuantity(availableQuantity)
                .build();
    }

    public static Registration activeRegistration(Long id, User user, Event event, TicketType ticketType) {
        return Registration.builder()
                .id(id)
                .user(user)
                .event(event)
                .ticketType(ticketType)
                .status(RegistrationStatus.ACTIVE)
                .build();
    }

    public static Payment paidPayment(Long id, Registration registration) {
        return Payment.builder()
                .id(id)
                .registration(registration)
                .amount(registration.getTicketType().getPrice())
                .status(PaymentStatus.PAID)
                .build();
    }

    public static Review review(Long id, User user, Event event) {
        return Review.builder()
                .id(id)
                .user(user)
                .event(event)
                .rating(5)
                .comment("Great event")
                .build();
    }

    public static CustomUserDetails userDetails(User user) {
        return new CustomUserDetails(user);
    }
}