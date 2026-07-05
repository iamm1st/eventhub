package com.eventhub.exception.category;

import com.eventhub.exception.ConflictException;

public class CategoryAlreadyExistsException extends ConflictException {

    public CategoryAlreadyExistsException(String name) {
        super("Category with name " + name + " already exists");
    }
}