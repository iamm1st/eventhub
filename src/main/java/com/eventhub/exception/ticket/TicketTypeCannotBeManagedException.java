package com.eventhub.exception.ticket;

import com.eventhub.exception.ConflictException;

public class TicketTypeCannotBeManagedException extends ConflictException {

    public TicketTypeCannotBeManagedException(Long eventId) {
        super("Ticket types can't be changed for event with id " + eventId);
    }
}