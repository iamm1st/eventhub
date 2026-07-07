package com.eventhub.exception.registration;

import com.eventhub.exception.ConflictException;

public class RegistrationAlreadyExistsException extends ConflictException {

    public RegistrationAlreadyExistsException(Long eventId) {
        super("User is already registered for event with id " + eventId);
    }
}