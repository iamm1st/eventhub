package com.eventhub.exception.review;

import com.eventhub.exception.ForbiddenActionException;

public class ReviewAccessDeniedException extends ForbiddenActionException {

    public ReviewAccessDeniedException(Long id) {
        super("You don't have permission to manage review with id " + id);
    }
}