package com.eventhub.exception.ticket;

import com.eventhub.exception.ConflictException;

public class TicketQuantityExceededException extends ConflictException {

    public TicketQuantityExceededException(Integer capacity) {
        super("Total ticket quantity can't be greater than event capacity " + capacity);
    }
}