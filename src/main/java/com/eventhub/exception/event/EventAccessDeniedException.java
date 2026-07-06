package com.eventhub.exception.event;

import com.eventhub.exception.ForbiddenActionException;

public class EventAccessDeniedException extends ForbiddenActionException {

    public EventAccessDeniedException() {
        super("You do not have permission to manage events");
    }

    public EventAccessDeniedException(Long id) {
        super("You do not have permission to manage event with id " + id);
    }
}