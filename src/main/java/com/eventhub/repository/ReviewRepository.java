package com.eventhub.repository;

import com.eventhub.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @EntityGraph(attributePaths = {"user", "event"})
    Page<Review> findByEventIdOrderByCreatedAtDesc(Long eventId, Pageable pageable);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.event.id = :eventId")
    Double calculateAverageRatingByEventId(@Param("eventId") Long eventId);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user", "event"})
    Optional<Review> findById(@NonNull Long id);
}