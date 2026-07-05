package com.eventhub.exception.user;

import com.eventhub.exception.BadRequestException;

public class AdminSelfBlockException extends BadRequestException {

    public AdminSelfBlockException() {
        super("Admin can't block own account");
    }
}