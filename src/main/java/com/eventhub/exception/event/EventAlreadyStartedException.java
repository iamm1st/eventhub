package com.eventhub.exception.event;

import com.eventhub.exception.ConflictException;

public class EventAlreadyStartedException extends ConflictException {

    public EventAlreadyStartedException(Long id) {
        super("Event with id " + id + " has already started");
    }
}