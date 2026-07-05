package com.eventhub.exception.organizer;

import com.eventhub.exception.ConflictException;

public class UserAlreadyOrganizerException extends ConflictException {

    public UserAlreadyOrganizerException(Long userId) {
        super("User with id " + userId + " is already an organizer");
    }
}