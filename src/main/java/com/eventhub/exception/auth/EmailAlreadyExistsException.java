package com.eventhub.exception.auth;

import com.eventhub.exception.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {

    public EmailAlreadyExistsException(String email) {
        super("User with email " + email + " already exists");
    }
}