package com.eventhub.exception.location;

import com.eventhub.exception.ResourceNotFoundException;

public class LocationNotFoundException extends ResourceNotFoundException {

    public LocationNotFoundException(Long id) {
        super("Location with id " + id + " not found");
    }
}