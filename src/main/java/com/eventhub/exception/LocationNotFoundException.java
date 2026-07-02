package com.eventhub.exception;

public class LocationNotFoundException extends ResourceNotFoundException {

    public LocationNotFoundException(Long id) {
        super("Location with id " + id + " not found");
    }
}