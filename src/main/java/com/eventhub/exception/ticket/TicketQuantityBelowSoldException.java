package com.eventhub.exception.ticket;

import com.eventhub.exception.ConflictException;

public class TicketQuantityBelowSoldException extends ConflictException {

    public TicketQuantityBelowSoldException(Long id, Integer soldQuantity) {
        super("Ticket type with id " + id + " can't have total quantity lower than sold quantity " + soldQuantity);
    }
}