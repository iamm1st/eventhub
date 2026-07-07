package com.eventhub.exception.registration;

import com.eventhub.exception.ResourceNotFoundException;

public class RegistrationNotFoundException extends ResourceNotFoundException {

    public RegistrationNotFoundException(Long id) {
        super("Registration with id " + id + " not found");
    }
}