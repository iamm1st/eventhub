package com.eventhub.exception.category;

import com.eventhub.exception.ResourceNotFoundException;

public class CategoryNotFoundException extends ResourceNotFoundException {

    public CategoryNotFoundException(Long id) {
        super("Category with id " + id + " not found");
    }
}