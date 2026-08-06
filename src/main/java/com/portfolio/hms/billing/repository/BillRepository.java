package com.portfolio.hms.billing.repository;

import com.portfolio.hms.billing.entity.Bill;
import com.portfolio.hms.billing.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BillRepository extends JpaRepository<Bill, UUID> {
    Optional<Bill> findByAppointmentId(UUID appointmentId);
    List<Bill> findByStatus(PaymentStatus status);
    long countByStatus(PaymentStatus status);
}
