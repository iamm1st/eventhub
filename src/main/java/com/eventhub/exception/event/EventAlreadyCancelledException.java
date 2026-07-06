package com.eventhub.exception.event;

import com.eventhub.exception.ConflictException;

public class EventAlreadyCancelledException extends ConflictException {

    public EventAlreadyCancelledException(Long id) {
        super("Event with id " + id + " is already cancelled");
    }
}