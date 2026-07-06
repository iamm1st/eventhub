package com.eventhub.exception.event;

import com.eventhub.exception.ConflictException;

public class EventCannotBeUpdatedException extends ConflictException {

    public EventCannotBeUpdatedException(Long id) {
        super("Event with id " + id + " can't be updated");
    }
}