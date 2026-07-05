package com.eventhub.exception.organizer;

import com.eventhub.exception.ConflictException;

public class OrganizerApplicationAlreadyExistsException extends ConflictException {

    public OrganizerApplicationAlreadyExistsException(Long userId) {
        super("User with id " + userId + " already has an active organizer application");
    }
}