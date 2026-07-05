package com.eventhub.exception.auth;

import com.eventhub.exception.ForbiddenActionException;

public class UserBlockedException extends ForbiddenActionException {

    public UserBlockedException(String email) {
        super("User with email " + email + " is blocked");
    }
}