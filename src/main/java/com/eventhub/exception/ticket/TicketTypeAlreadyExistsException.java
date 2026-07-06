package com.eventhub.exception.ticket;

import com.eventhub.exception.ConflictException;

public class TicketTypeAlreadyExistsException extends ConflictException {

    public TicketTypeAlreadyExistsException(String name) {
        super("Ticket type with name " + name + " already exists for this event");
    }
}