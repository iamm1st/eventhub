package com.eventhub.exception.user;

import com.eventhub.exception.ConflictException;

public class UserAlreadyBlockedException extends ConflictException {

    public UserAlreadyBlockedException(Long id) {
        super("User with id " + id + " is already blocked");
    }
}