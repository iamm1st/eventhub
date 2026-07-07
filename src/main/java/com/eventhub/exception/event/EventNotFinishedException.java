package com.eventhub.exception.event;

import com.eventhub.exception.ConflictException;

public class EventNotFinishedException extends ConflictException {

    public EventNotFinishedException(Long id) {
        super("Event with id " + id + " isn't finished yet");
    }
}