package com.eventhub.exception.review;

import com.eventhub.exception.ResourceNotFoundException;

public class ReviewNotFoundException extends ResourceNotFoundException {

    public ReviewNotFoundException(Long id) {
        super("Review with id " + id + " not found");
    }
}