package com.eventhub.exception.event;

import com.eventhub.exception.ConflictException;

public class EventCannotBeDeletedException extends ConflictException {

    public EventCannotBeDeletedException(Long id) {
        super("Event with id " + id + " can't be deleted. Published events should be cancelled instead");
    }
}