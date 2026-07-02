package com.eventhub.exception;

public class CategoryAlreadyExistsException extends ConflictException {

    public CategoryAlreadyExistsException(String name) {
        super("Category with name " + name + " already exists");
    }
}