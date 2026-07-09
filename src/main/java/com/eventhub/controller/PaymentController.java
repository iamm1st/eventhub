package com.eventhub.controller;

import com.eventhub.dto.response.PaymentResponse;
import com.eventhub.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/admin/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @ParameterObject
            @PageableDefault(sort = "createdAt")
            Pageable pageable) {
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }

    @GetMapping("/admin/payments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/organizer/payments")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Page<PaymentResponse>> getOrganizerPayments(
            @ParameterObject
            @PageableDefault(sort = "createdAt")
            Pageable pageable) {
        return ResponseEntity.ok(paymentService.getOrganizerPayments(pageable));
    }
}