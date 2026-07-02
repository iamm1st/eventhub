package com.eventhub.repository;

import com.eventhub.entity.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventCategoryRepository extends JpaRepository<EventCategory, Long> {

    // check the uniqueness of the name excluding a register
    Optional<EventCategory> findByNameIgnoreCase(String name);

    // quick checking during creation
    boolean existsByNameIgnoreCase(String name);
}