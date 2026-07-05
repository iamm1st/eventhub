package com.eventhub.exception.user;

import com.eventhub.exception.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(Long id) {
        super("User with id " + id + " not found");
    }
}