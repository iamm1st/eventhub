package com.eventhub.repository;

import com.eventhub.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    boolean existsByCategoryId(Long categoryId);

    boolean existsByLocationId(Long locationId);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"organizer", "category", "location"})
    Optional<Event> findById(@NonNull Long id);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"organizer", "category", "location"})
    Page<Event> findAll(@Nullable Specification<Event> specification, @NonNull Pageable pageable);
}