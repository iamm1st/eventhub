package com.eventhub.exception.event;

import com.eventhub.exception.ConflictException;

public class EventCannotBePublishedException extends ConflictException {

    public EventCannotBePublishedException(Long id) {
        super("Event with id " + id + " can't be published");
    }
}