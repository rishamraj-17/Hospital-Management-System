package com.portfolio.hms.clinical.controller;

import com.portfolio.hms.appointment.service.AppointmentService;
import com.portfolio.hms.clinical.dto.DoctorRequest;
import com.portfolio.hms.clinical.dto.DoctorResponse;
import com.portfolio.hms.clinical.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring managed dependency")
    private final DoctorService doctorService;
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring managed dependency")
    private final AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<Page<DoctorResponse>> searchDoctors(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String departmentName,
            Pageable pageable) {
        return ResponseEntity.ok(doctorService.searchDoctors(specialization, departmentName, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable UUID id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody DoctorRequest request) {
        return new ResponseEntity<>(doctorService.createDoctor(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable UUID id,
            @Valid @RequestBody DoctorRequest request) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteDoctor(@PathVariable UUID id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<java.util.List<com.portfolio.hms.appointment.dto.DoctorScheduleResponse>> getDoctorSchedule(
            @PathVariable UUID id,
            @RequestParam java.time.LocalDate date) {
        var startOfDay = date.atStartOfDay();
        var endOfDay = date.atTime(23, 59, 59);
        var appointments = appointmentService.getDoctorAppointments(id, startOfDay, endOfDay);

        var response = appointments.stream().map(a -> new com.portfolio.hms.appointment.dto.DoctorScheduleResponse(
                a.getStartTime().toLocalTime(),
                a.getStartTime().plusMinutes(a.getDurationMinutes()).toLocalTime(),
                a.getStatus().name()
        )).toList();

        return ResponseEntity.ok(response);
    }
}
