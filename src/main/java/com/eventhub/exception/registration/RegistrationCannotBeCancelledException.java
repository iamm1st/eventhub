package com.eventhub.exception.registration;

import com.eventhub.exception.ConflictException;

public class RegistrationCannotBeCancelledException extends ConflictException {

    public RegistrationCannotBeCancelledException(Long id) {
        super("Registration with id " + id + " can't be cancelled");
    }
}