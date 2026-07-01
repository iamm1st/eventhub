package com.eventhub.exception;

public class UsernameAlreadyExistsException extends ConflictException {

    public UsernameAlreadyExistsException(String username) {
        super("User with username " + username + " already exists");
    }
}