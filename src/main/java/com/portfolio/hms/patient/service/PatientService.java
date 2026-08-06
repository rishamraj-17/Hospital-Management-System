package com.portfolio.hms.patient.service;

import com.portfolio.hms.common.dto.PageResponse;
import com.portfolio.hms.exception.EmailAlreadyExistsException;
import com.portfolio.hms.exception.PatientNotFoundException;
import com.portfolio.hms.patient.dto.PatientRequest;
import com.portfolio.hms.patient.dto.PatientResponse;
import com.portfolio.hms.patient.entity.Patient;
import com.portfolio.hms.patient.repository.PatientRepository;
import com.portfolio.hms.patient.repository.PatientSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;

    @Transactional(readOnly = true)
    public PageResponse<PatientResponse> searchPatients(String name, String bloodGroup, Pageable pageable) {
        log.debug("Searching patients by name: {} and bloodGroup: {}", name, bloodGroup);
        Specification<Patient> spec = PatientSpecification.search(name, bloodGroup);
        Page<Patient> page = patientRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(UUID id) {
        return mapToResponse(findPatientEntity(id));
    }

    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        if (request.email() != null && patientRepository.existsByEmail(request.email())) {
            log.warn("Attempt to create patient with existing email: {}", request.email());
            throw new EmailAlreadyExistsException("Patient with email " + request.email() + " already exists");
        }

        String mrn = generateMrn();
        log.info("Generated MRN {} for new patient {} {}", mrn, request.firstName(), request.lastName());

        Patient patient = Patient.builder()
                .mrn(mrn)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .bloodGroup(request.bloodGroup())
                .phone(request.phone())
                .email(request.email())
                .address(request.address())
                .emergencyContactName(request.emergencyContactName())
                .emergencyContactPhone(request.emergencyContactPhone())
                .emergencyContactRelationship(request.emergencyContactRelationship())
                .build();

        return mapToResponse(patientRepository.save(patient));
    }

    @Transactional
    public PatientResponse updatePatient(UUID id, PatientRequest request) {
        Patient patient = findPatientEntity(id);

        if (request.email() != null && patientRepository.existsByEmailAndIdNot(request.email(), id)) {
            log.warn("Attempt to update patient with existing email: {}", request.email());
            throw new EmailAlreadyExistsException("Patient with email " + request.email() + " already exists");
        }

        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setBloodGroup(request.bloodGroup());
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setAddress(request.address());
        patient.setEmergencyContactName(request.emergencyContactName());
        patient.setEmergencyContactPhone(request.emergencyContactPhone());
        patient.setEmergencyContactRelationship(request.emergencyContactRelationship());

        return mapToResponse(patientRepository.save(patient));
    }

    @Transactional
    public void deletePatient(UUID id) {
        log.info("Deleting patient with id: {}", id);
        Patient patient = findPatientEntity(id);
        patientRepository.delete(patient);
    }

    public Patient findPatientEntity(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));
    }

    private String generateMrn() {
        int currentYear = Year.now().getValue();
        Long sequence = patientRepository.getNextMrnSequence();
        return String.format("MRN-%d-%06d", currentYear, sequence);
    }

    private PatientResponse mapToResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getMrn(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getBloodGroup(),
                patient.getPhone(),
                patient.getEmail(),
                patient.getAddress(),
                patient.getEmergencyContactName(),
                patient.getEmergencyContactPhone(),
                patient.getEmergencyContactRelationship()
        );
    }
}
