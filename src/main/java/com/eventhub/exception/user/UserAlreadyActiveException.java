package com.eventhub.exception.user;

import com.eventhub.exception.ConflictException;

public class UserAlreadyActiveException extends ConflictException {

    public UserAlreadyActiveException(Long id) {
        super("User with id " + id + " is already active");
    }
}