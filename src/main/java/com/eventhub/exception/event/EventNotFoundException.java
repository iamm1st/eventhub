package com.eventhub.exception.event;

import com.eventhub.exception.ResourceNotFoundException;

public class EventNotFoundException extends ResourceNotFoundException {

    public EventNotFoundException(Long id) {
        super("Event with id " + id + " not found");
    }
}