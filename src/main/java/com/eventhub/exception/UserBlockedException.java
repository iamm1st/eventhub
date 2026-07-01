package com.eventhub.exception;

public class UserBlockedException extends ForbiddenActionException {

    public UserBlockedException(String email) {
        super("User with email " + email + " is blocked");
    }
}