package com.eventhub.exception.event;

import com.eventhub.exception.ConflictException;

public class EventNotPublishedException extends ConflictException {

    public EventNotPublishedException(Long id) {
        super("Event with id " + id + " isn't published");
    }
}