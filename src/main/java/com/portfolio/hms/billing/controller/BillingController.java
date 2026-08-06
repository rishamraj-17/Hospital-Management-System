package com.portfolio.hms.billing.controller;

import com.portfolio.hms.billing.dto.BillRequest;
import com.portfolio.hms.billing.dto.BillResponse;
import com.portfolio.hms.billing.dto.PaymentRequest;
import com.portfolio.hms.billing.dto.PaymentResponse;
import com.portfolio.hms.billing.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/bills")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BillResponse> generateBill(@Valid @RequestBody BillRequest request) {
        return new ResponseEntity<>(billingService.generateBill(request), HttpStatus.CREATED);
    }

    @GetMapping("/bills/{id}")
    public ResponseEntity<BillResponse> getBill(@PathVariable UUID id) {
        return ResponseEntity.ok(billingService.getBill(id));
    }

    @GetMapping("/appointments/{appointmentId}/bill")
    public ResponseEntity<BillResponse> getBillByAppointment(@PathVariable UUID appointmentId) {
        return ResponseEntity.ok(billingService.getBillByAppointment(appointmentId));
    }

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        return new ResponseEntity<>(billingService.processPayment(request), HttpStatus.CREATED);
    }

    @GetMapping("/bills/{billId}/payments")
    public ResponseEntity<List<PaymentResponse>> getPaymentsForBill(@PathVariable UUID billId) {
        return ResponseEntity.ok(billingService.getPaymentsForBill(billId));
    }

    @PostMapping("/bills/{id}/refund")
    public ResponseEntity<BillResponse> refundBill(@PathVariable UUID id) {
        return ResponseEntity.ok(billingService.refundBill(id));
    }

    @PostMapping("/bills/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> cancelBill(@PathVariable UUID id) {
        billingService.cancelBill(id);
        return ResponseEntity.noContent().build();
    }
}
