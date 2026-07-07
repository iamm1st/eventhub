package com.eventhub.repository;

import com.eventhub.entity.TicketType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    @EntityGraph(attributePaths = "event")
    List<TicketType> findByEventIdOrderByPriceAsc(Long eventId);

    // duplicate check
    boolean existsByEventIdAndNameIgnoreCase(Long eventId, String name);

    boolean existsByEventIdAndNameIgnoreCaseAndIdNot(Long eventId, String name, Long id);

    @Query("SELECT COALESCE(SUM(t.totalQuantity), 0) FROM TicketType t WHERE t.event.id = :eventId")
    Long sumTotalQuantityByEventId(@Param("eventId") Long eventId);

    @Query("""
            SELECT COALESCE(SUM(t.totalQuantity), 0)
            FROM TicketType t
            WHERE t.event.id = :eventId AND t.id <> :ticketTypeId
            """)

    // needed when updating
    Long sumTotalQuantityByEventIdExcludingTicketType(@Param("eventId") Long eventId, @Param("ticketTypeId") Long ticketTypeId);

    // avoid a situation where 2 users buy the last ticket at the same time
    // block a row in the db until the end of the transaction
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"event", "event.organizer"})
    @Query("SELECT t FROM TicketType t WHERE t.id = :id")
    Optional<TicketType> findByIdForUpdate(@Param("id") Long id);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"event", "event.organizer"})
    Optional<TicketType> findById(@NonNull Long id);
}