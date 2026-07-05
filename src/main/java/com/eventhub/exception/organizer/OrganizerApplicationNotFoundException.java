package com.eventhub.exception.organizer;

import com.eventhub.exception.ResourceNotFoundException;

public class OrganizerApplicationNotFoundException extends ResourceNotFoundException {

    public OrganizerApplicationNotFoundException(Long id) {
        super("Organizer application with id " + id + " not found");
    }
}