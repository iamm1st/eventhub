package com.eventhub.exception.event;

import com.eventhub.exception.ConflictException;

public class EventCapacityBelowTicketQuantityException extends ConflictException {

    public EventCapacityBelowTicketQuantityException(Long eventId) {
        super("Event with id " + eventId + " capacity can't be lower than existing ticket quantity");
    }
}