package com.eventhub.exception.event;

import com.eventhub.exception.BadRequestException;

public class InvalidEventDatesException extends BadRequestException {

    public InvalidEventDatesException() {
        super("Event end date must be after start date");
    }
}