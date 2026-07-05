package com.eventhub.exception.organizer;

import com.eventhub.exception.ConflictException;

public class OrganizerApplicationAlreadyReviewedException extends ConflictException {

    public OrganizerApplicationAlreadyReviewedException(Long id) {
        super("Organizer application with id " + id + " has already been reviewed");
    }
}