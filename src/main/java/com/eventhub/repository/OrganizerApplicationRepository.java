package com.eventhub.repository;

import com.eventhub.entity.OrganizerApplication;
import com.eventhub.enums.OrganizerApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface OrganizerApplicationRepository extends JpaRepository<OrganizerApplication, Long> {

    boolean existsByUserIdAndStatusIn(Long userId, Collection<OrganizerApplicationStatus> statuses);

    @EntityGraph(attributePaths = "user")
    Page<OrganizerApplication> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Page<OrganizerApplication> findByStatus(OrganizerApplicationStatus status, Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = "user")
    Page<OrganizerApplication> findAll(@NonNull Pageable pageable);

    @Override
    @NonNull
    @EntityGraph(attributePaths = "user")
    Optional<OrganizerApplication> findById(@NonNull Long id);
}