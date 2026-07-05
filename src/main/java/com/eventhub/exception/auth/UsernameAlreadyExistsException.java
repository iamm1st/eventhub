package com.eventhub.exception.auth;

import com.eventhub.exception.ConflictException;

public class UsernameAlreadyExistsException extends ConflictException {

    public UsernameAlreadyExistsException(String username) {
        super("User with username " + username + " already exists");
    }
}