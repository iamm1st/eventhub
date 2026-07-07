package com.eventhub.exception.review;

import com.eventhub.exception.ForbiddenActionException;

public class ReviewNotAllowedException extends ForbiddenActionException {

    public ReviewNotAllowedException(Long eventId) {
        super("User can't review event with id " + eventId + " because there is no active registration");
    }
}