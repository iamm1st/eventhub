package com.eventhub.controller;

import com.eventhub.dto.response.PaymentResponse;
import com.eventhub.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Payments", description = "Payment query endpoints for admins and organizers")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Get all payments", description = "Returns paginated payments for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payments returned successfully"),
            @ApiResponse(responseCode = "403", description = "Admin role required")})
    @GetMapping("/admin/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @ParameterObject
            @PageableDefault(sort = "createdAt")
            Pageable pageable) {
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }

    @Operation(summary = "Get payment by id", description = "Returns payment by id for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "403", description = "Admin role required"),
            @ApiResponse(responseCode = "404", description = "Payment not found")})
    @GetMapping("/admin/payments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @Operation(summary = "Get organizer payments", description = "Returns payments for current organizer events")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organizer payments returned successfully"),
            @ApiResponse(responseCode = "403", description = "Organizer role required")})
    @GetMapping("/organizer/payments")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Page<PaymentResponse>> getOrganizerPayments(
            @ParameterObject
            @PageableDefault(sort = "createdAt")
            Pageable pageable) {
        return ResponseEntity.ok(paymentService.getOrganizerPayments(pageable));
    }
}