package com.eventhub.repository;

import com.eventhub.entity.Registration;
import com.eventhub.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Checking the ticket type deletion
@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    boolean existsByTicketTypeIdAndStatus(Long ticketTypeId, RegistrationStatus status);
}