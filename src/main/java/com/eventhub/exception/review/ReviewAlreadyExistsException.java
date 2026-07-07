package com.eventhub.exception.review;

import com.eventhub.exception.ConflictException;

public class ReviewAlreadyExistsException extends ConflictException {

    public ReviewAlreadyExistsException(Long eventId) {
        super("User has already reviewed event with id " + eventId);
    }
}