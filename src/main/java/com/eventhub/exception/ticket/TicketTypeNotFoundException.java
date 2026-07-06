package com.eventhub.exception.ticket;

import com.eventhub.exception.ResourceNotFoundException;

public class TicketTypeNotFoundException extends ResourceNotFoundException {

    public TicketTypeNotFoundException(Long id) {
        super("Ticket type with id " + id + " not found");
    }
}