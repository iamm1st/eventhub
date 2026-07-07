package com.eventhub.exception.registration;

import com.eventhub.exception.ForbiddenActionException;

public class RegistrationAccessDeniedException extends ForbiddenActionException {

    public RegistrationAccessDeniedException(Long id) {
        super("You don't have permission to manage registration with id " + id);
    }
}