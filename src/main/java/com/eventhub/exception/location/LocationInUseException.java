package com.eventhub.exception.location;

import com.eventhub.exception.ConflictException;

public class LocationInUseException extends ConflictException {

    public LocationInUseException(Long id) {
        super("Location with id " + id + " cannot be deleted because it is used by events");
    }
}