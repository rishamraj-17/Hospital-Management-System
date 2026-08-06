package com.portfolio.hms.billing.service;

import com.portfolio.hms.appointment.entity.Appointment;
import com.portfolio.hms.appointment.enums.AppointmentStatus;
import com.portfolio.hms.appointment.repository.AppointmentRepository;
import com.portfolio.hms.billing.dto.BillRequest;
import com.portfolio.hms.billing.dto.BillResponse;
import com.portfolio.hms.billing.dto.PaymentRequest;
import com.portfolio.hms.billing.dto.PaymentResponse;
import com.portfolio.hms.billing.entity.Bill;
import com.portfolio.hms.billing.entity.Payment;
import com.portfolio.hms.billing.enums.PaymentStatus;
import com.portfolio.hms.billing.repository.BillRepository;
import com.portfolio.hms.billing.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public BillResponse generateBill(BillRequest request) {
        log.info("Generating bill for appointment {}", request.appointmentId());

        Appointment appointment = appointmentRepository.findById(request.appointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Bills can only be generated for COMPLETED appointments");
        }

        if (billRepository.findByAppointmentId(appointment.getId()).isPresent()) {
            throw new IllegalStateException("A bill already exists for this appointment");
        }

        Bill bill = Bill.builder()
                .appointment(appointment)
                .consultationFee(request.consultationFee())
                .medicineCharges(request.medicineCharges())
                .additionalCharges(request.additionalCharges())
                .status(PaymentStatus.PENDING)
                .build();

        bill.calculateTotal();

        Bill savedBill = billRepository.save(bill);
        return mapToBillResponse(savedBill);
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for bill {}", request.billId());

        Bill bill = billRepository.findById(request.billId())
                .orElseThrow(() -> new IllegalArgumentException("Bill not found"));

        if (bill.getStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Bill is already fully paid. Cannot process duplicate payment.");
        }

        if (bill.getStatus() == PaymentStatus.CANCELLED || bill.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Cannot process payment for a " + bill.getStatus() + " bill.");
        }

        if (request.amount().compareTo(bill.getTotalAmount()) < 0) {
            throw new IllegalArgumentException("Partial payments are not supported at this time.");
        }

        if (request.amount().compareTo(bill.getTotalAmount()) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds total bill amount.");
        }

        Payment payment = Payment.builder()
                .bill(bill)
                .amount(request.amount())
                .paymentMethod(request.paymentMethod())
                .transactionReference(request.transactionReference())
                .paymentDate(Instant.now())
                .build();

        paymentRepository.save(payment);

        bill.setStatus(PaymentStatus.PAID);
        billRepository.save(bill);

        return mapToPaymentResponse(payment);
    }

    @Transactional
    public BillResponse refundBill(UUID billId) {
        log.info("Refunding bill {}", billId);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found"));

        if (bill.getStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Only PAID bills can be refunded");
        }

        bill.setStatus(PaymentStatus.REFUNDED);
        Bill savedBill = billRepository.save(bill);

        return mapToBillResponse(savedBill);
    }

    @Transactional
    public void cancelBill(UUID billId) {
        log.info("Cancelling bill {}", billId);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found"));

        if (bill.getStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Paid bills cannot be cancelled, they must be refunded.");
        }

        bill.setStatus(PaymentStatus.CANCELLED);
        billRepository.save(bill);
    }

    @Transactional(readOnly = true)
    public BillResponse getBill(UUID id) {
        return billRepository.findById(id)
                .map(this::mapToBillResponse)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found"));
    }

    @Transactional(readOnly = true)
    public BillResponse getBillByAppointment(UUID appointmentId) {
        return billRepository.findByAppointmentId(appointmentId)
                .map(this::mapToBillResponse)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found for appointment"));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsForBill(UUID billId) {
        return paymentRepository.findByBillId(billId).stream()
                .map(this::mapToPaymentResponse)
                .toList();
    }

    private BillResponse mapToBillResponse(Bill bill) {
        return new BillResponse(
                bill.getId(),
                bill.getAppointment().getId(),
                bill.getConsultationFee(),
                bill.getMedicineCharges(),
                bill.getAdditionalCharges(),
                bill.getTotalAmount(),
                bill.getStatus().name()
        );
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBill().getId(),
                payment.getAmount(),
                payment.getPaymentMethod().name(),
                payment.getTransactionReference(),
                payment.getPaymentDate()
        );
    }
}
