package com.eventhub.exception.payment;

import com.eventhub.exception.ResourceNotFoundException;

public class PaymentNotFoundException extends ResourceNotFoundException {

    public PaymentNotFoundException(Long registrationId) {
        super("Payment for registration with id " + registrationId + " not found");
    }
}