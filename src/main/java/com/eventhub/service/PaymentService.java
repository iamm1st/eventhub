package com.eventhub.service;

import com.eventhub.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    Page<PaymentResponse> getAllPayments(Pageable pageable);

    PaymentResponse getPaymentById(Long id);

    Page<PaymentResponse> getOrganizerPayments(Pageable pageable);
}