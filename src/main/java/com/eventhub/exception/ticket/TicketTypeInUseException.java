package com.eventhub.exception.ticket;

import com.eventhub.exception.ConflictException;

public class TicketTypeInUseException extends ConflictException {

    public TicketTypeInUseException(Long id) {
        super("Ticket type with id " + id + " can't be deleted because it has active registrations");
    }
}