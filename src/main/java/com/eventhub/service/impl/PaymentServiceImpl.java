package com.eventhub.service.impl;

import com.eventhub.dto.response.PaymentResponse;
import com.eventhub.entity.Payment;
import com.eventhub.exception.payment.PaymentNotFoundException;
import com.eventhub.mapper.PaymentMapper;
import com.eventhub.repository.PaymentRepository;
import com.eventhub.security.CurrentUserProvider;
import com.eventhub.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(paymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getOrganizerPayments(Pageable pageable) {
        Long organizerId = currentUserProvider.getCurrentUserId();

        return paymentRepository.findByRegistrationEventOrganizerIdOrderByCreatedAtDesc(organizerId, pageable).map(paymentMapper::toResponse);
    }
}