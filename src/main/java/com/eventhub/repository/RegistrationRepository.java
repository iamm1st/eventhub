package com.eventhub.repository;

import com.eventhub.entity.Registration;
import com.eventhub.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    boolean existsByTicketTypeIdAndStatus(Long ticketTypeId, RegistrationStatus status);

    boolean existsByUserIdAndEventIdAndStatus(Long userId, Long eventId, RegistrationStatus status);

    // how many tickets have the organizer sold
    long countByEventOrganizerIdAndStatus(Long organizerId, RegistrationStatus status);

    @EntityGraph(attributePaths = {"event", "ticketType"})
    List<Registration> findByUserIdOrderByRegistrationDateDesc(Long userId);

    // list of participants of the event for organizer
    @EntityGraph(attributePaths = {"user", "ticketType"})
    List<Registration> findByEventIdOrderByRegistrationDateDesc(Long eventId);

    // to find the most popular event using stream API
    @EntityGraph(attributePaths = {"event", "ticketType"})
    List<Registration> findByEventOrganizerIdAndStatus(Long organizerId, RegistrationStatus status);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user", "event", "event.organizer", "ticketType"})
    Optional<Registration> findById(@NonNull Long id);
}