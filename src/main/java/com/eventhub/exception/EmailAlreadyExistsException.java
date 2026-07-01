package com.eventhub.exception;

public class EmailAlreadyExistsException extends ConflictException {

    public EmailAlreadyExistsException(String email) {
        super("User with email " + email + " already exists");
    }
}