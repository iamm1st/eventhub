package com.eventhub.exception.category;

import com.eventhub.exception.ConflictException;

public class CategoryInUseException extends ConflictException {

    public CategoryInUseException(Long id) {
        super("Category with id " + id + " can't be deleted because it is used by events");
    }
}