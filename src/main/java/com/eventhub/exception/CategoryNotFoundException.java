package com.eventhub.exception;

public class CategoryNotFoundException extends ResourceNotFoundException {

    public CategoryNotFoundException(Long id) {
        super("Category with id " + id + " not found");
    }
}