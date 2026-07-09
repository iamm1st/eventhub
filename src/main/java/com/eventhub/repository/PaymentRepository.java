package com.eventhub.repository;

import com.eventhub.entity.Payment;
import com.eventhub.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRegistrationId(Long registrationId);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {
            "registration",
            "registration.user",
            "registration.event",
            "registration.ticketType"
    })

    Page<Payment> findAll(@NonNull Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {
            "registration",
            "registration.user",
            "registration.event",
            "registration.ticketType"
    })

    Optional<Payment> findById(@NonNull Long id);

    @EntityGraph(attributePaths = {
            "registration",
            "registration.user",
            "registration.event",
            "registration.ticketType"
    })

    // organizer sees payments only for their events
    Page<Payment> findByRegistrationEventOrganizerIdOrderByCreatedAtDesc(Long organizerId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    // total profit of the platform
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.status = :status
              AND p.registration.event.organizer.id = :organizerId
            """)
    BigDecimal sumAmountByStatusAndOrganizerId(
            @Param("status") PaymentStatus status,
            @Param("organizerId") Long organizerId);
}