package com.eventhub.exception.registration;

import com.eventhub.exception.ConflictException;

public class TicketUnavailableException extends ConflictException {

    public TicketUnavailableException(Long ticketTypeId) {
        super("Ticket type with id " + ticketTypeId + " is unavailable");
    }
}