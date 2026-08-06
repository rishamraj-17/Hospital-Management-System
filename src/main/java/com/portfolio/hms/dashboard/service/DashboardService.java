package com.portfolio.hms.dashboard.service;

import com.portfolio.hms.appointment.repository.AppointmentRepository;
import com.portfolio.hms.billing.enums.PaymentStatus;
import com.portfolio.hms.billing.repository.BillRepository;
import com.portfolio.hms.clinical.repository.DepartmentRepository;
import com.portfolio.hms.clinical.repository.DoctorRepository;
import com.portfolio.hms.dashboard.dto.DashboardResponse;
import com.portfolio.hms.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillRepository billRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getAdminDashboard() {
        long totalDoctors = doctorRepository.count();
        long activeDepartments = departmentRepository.count();
        long totalPatients = patientRepository.count();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        long appointmentsToday = appointmentRepository.countByStartTimeBetween(startOfDay, endOfDay);

        long pendingBills = billRepository.countByStatus(PaymentStatus.PENDING);

        return new DashboardResponse(
                totalDoctors,
                totalPatients,
                appointmentsToday,
                activeDepartments,
                pendingBills
        );
    }
}
