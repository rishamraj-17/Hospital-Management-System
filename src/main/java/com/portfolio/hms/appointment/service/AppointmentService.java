package com.portfolio.hms.appointment.service;

import com.portfolio.hms.appointment.dto.AppointmentRequest;
import com.portfolio.hms.appointment.dto.AppointmentResponse;
import com.portfolio.hms.appointment.entity.Appointment;
import com.portfolio.hms.appointment.enums.AppointmentStatus;
import com.portfolio.hms.appointment.event.AppointmentScheduledEvent;
import com.portfolio.hms.appointment.metrics.AppointmentMetrics;
import com.portfolio.hms.appointment.repository.AppointmentRepository;
import com.portfolio.hms.clinical.entity.Doctor;
import com.portfolio.hms.clinical.repository.DoctorRepository;
import com.portfolio.hms.exception.DoctorNotFoundException;
import com.portfolio.hms.exception.PatientNotFoundException;
import com.portfolio.hms.patient.entity.Patient;
import com.portfolio.hms.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AppointmentMetrics metrics;

    @Transactional
    public AppointmentResponse scheduleAppointment(AppointmentRequest request) {
        log.info("Attempting to schedule appointment for patient {} with doctor {}", request.patientId(), request.doctorId());

        // 1. Verify patient exists
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + request.patientId()));

        // 2. Verify doctor exists
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + request.doctorId()));

        // 3. Verify doctor is active
        // Soft delete handles this (doctor wouldn't be found if deleted)

        // 4. Verify appointment is in the future
        if (request.startTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment start time must be in the future");
        }

        // 5. Verify doctor is available during that time (simplified for now)

        // 6. Verify appointment does not overlap another appointment
        LocalDateTime newStart = request.startTime();
        LocalDateTime newEnd = newStart.plusMinutes(request.durationMinutes());

        List<Appointment> dailyAppointments = appointmentRepository.findByDoctorIdAndStartTimeBetweenOrderByStartTimeAsc(
                doctor.getId(),
                newStart.toLocalDate().atStartOfDay(),
                newStart.toLocalDate().atTime(23, 59, 59)
        );

        boolean hasOverlap = dailyAppointments.stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED && a.getStatus() != AppointmentStatus.NO_SHOW)
                .anyMatch(existing -> {
                    LocalDateTime existingStart = existing.getStartTime();
                    LocalDateTime existingEnd = existingStart.plusMinutes(existing.getDurationMinutes());
                    // overlap logic: existing.start < new.end && new.start < existing.end
                    return existingStart.isBefore(newEnd) && newStart.isBefore(existingEnd);
                });

        if (hasOverlap) {
            metrics.incrementConflicts();
            log.warn("Appointment overlap detected for doctor {} at {}", doctor.getId(), newStart);
            throw new IllegalStateException("Doctor is already booked for the requested time slot");
        }

        // 7. Persist inside one transaction
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .startTime(newStart)
                .durationMinutes(request.durationMinutes())
                .status(AppointmentStatus.SCHEDULED)
                .build();

        appointment = appointmentRepository.save(appointment);

        // 8. Publish Domain Event
        eventPublisher.publishEvent(new AppointmentScheduledEvent(
                appointment.getId(), patient.getId(), doctor.getId()
        ));

        // 9. Update Metrics
        metrics.incrementScheduled();

        return mapToResponse(appointment);
    }

    @Transactional
    public AppointmentResponse confirmAppointment(UUID id) {
        Appointment appointment = findAppointmentEntity(id);
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Only SCHEDULED appointments can be CONFIRMED");
        }
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return mapToResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse completeAppointment(UUID id) {
        Appointment appointment = findAppointmentEntity(id);
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED && appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Only SCHEDULED or CONFIRMED appointments can be COMPLETED");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        metrics.incrementCompleted();
        return mapToResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse cancelAppointment(UUID id) {
        Appointment appointment = findAppointmentEntity(id);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel an appointment that is already COMPLETED or CANCELLED");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        metrics.incrementCancelled();
        return mapToResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse markNoShow(UUID id) {
        Appointment appointment = findAppointmentEntity(id);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot mark NO_SHOW for an appointment that is already COMPLETED or CANCELLED");
        }
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        metrics.incrementNoShow();
        return mapToResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse rescheduleAppointment(UUID id, LocalDateTime newStartTime) {
        Appointment appointment = findAppointmentEntity(id);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot reschedule an appointment that is already COMPLETED or CANCELLED");
        }

        if (newStartTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("New start time must be in the future");
        }

        LocalDateTime newEnd = newStartTime.plusMinutes(appointment.getDurationMinutes());
        List<Appointment> dailyAppointments = appointmentRepository.findByDoctorIdAndStartTimeBetweenOrderByStartTimeAsc(
                appointment.getDoctor().getId(),
                newStartTime.toLocalDate().atStartOfDay(),
                newStartTime.toLocalDate().atTime(23, 59, 59)
        );

        boolean hasOverlap = dailyAppointments.stream()
                .filter(a -> !a.getId().equals(appointment.getId())) // exclude self
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED && a.getStatus() != AppointmentStatus.NO_SHOW)
                .anyMatch(existing -> {
                    LocalDateTime existingStart = existing.getStartTime();
                    LocalDateTime existingEnd = existingStart.plusMinutes(existing.getDurationMinutes());
                    return existingStart.isBefore(newEnd) && newStartTime.isBefore(existingEnd);
                });

        if (hasOverlap) {
            metrics.incrementConflicts();
            throw new IllegalStateException("Doctor is already booked for the requested new time slot");
        }

        appointment.setStartTime(newStartTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED); // Reset status on reschedule
        return mapToResponse(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<Appointment> getDoctorAppointments(UUID doctorId, LocalDateTime dayStart, LocalDateTime dayEnd) {
        return appointmentRepository.findByDoctorIdAndStartTimeBetweenOrderByStartTimeAsc(doctorId, dayStart, dayEnd);
    }

    private Appointment findAppointmentEntity(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with id: " + id));
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getDoctor().getId(),
                appointment.getStartTime(),
                appointment.getDurationMinutes(),
                appointment.getStartTime().plusMinutes(appointment.getDurationMinutes()),
                appointment.getStatus()
        );
    }
}
