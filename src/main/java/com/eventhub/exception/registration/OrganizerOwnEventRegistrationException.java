package com.eventhub.exception.registration;

import com.eventhub.exception.ConflictException;

public class OrganizerOwnEventRegistrationException extends ConflictException {

    public OrganizerOwnEventRegistrationException(Long eventId) {
        super("Organizer can't buy tickets for own event with id " + eventId);
    }
}