package com.eventhub.exception;

public class UserBlockedException extends RuntimeException {

    public UserBlockedException(String email) {
        super("User with email " + email + " is blocked");
    }
}