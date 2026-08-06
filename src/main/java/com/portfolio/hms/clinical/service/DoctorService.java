package com.portfolio.hms.clinical.service;

import com.portfolio.hms.auth.entity.User;
import com.portfolio.hms.auth.repository.UserRepository;
import com.portfolio.hms.clinical.dto.DoctorRequest;
import com.portfolio.hms.clinical.dto.DoctorResponse;
import com.portfolio.hms.clinical.entity.Department;
import com.portfolio.hms.clinical.entity.Doctor;
import com.portfolio.hms.clinical.repository.DepartmentRepository;
import com.portfolio.hms.clinical.repository.DoctorRepository;
import com.portfolio.hms.clinical.repository.DoctorSpecification;
import com.portfolio.hms.exception.DepartmentNotFoundException;
import com.portfolio.hms.exception.DoctorAlreadyExistsException;
import com.portfolio.hms.exception.DoctorNotFoundException;
import com.portfolio.hms.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public Page<DoctorResponse> searchDoctors(String specialization, String departmentName, Pageable pageable) {
        Specification<Doctor> spec = Specification.where(DoctorSpecification.hasSpecialization(specialization))
                .and(DoctorSpecification.hasDepartmentName(departmentName));
        return doctorRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(UUID id) {
        return mapToResponse(findDoctorEntity(id));
    }

    @Transactional
    public DoctorResponse createDoctor(DoctorRequest request) {
        if (doctorRepository.existsByLicenseNumber(request.licenseNumber())) {
            throw new DoctorAlreadyExistsException("Doctor with license number " + request.licenseNumber() + " already exists");
        }
        if (doctorRepository.existsByEmail(request.email())) {
            throw new DoctorAlreadyExistsException("Doctor with email " + request.email() + " already exists");
        }
        if (doctorRepository.existsByUserId(request.userId())) {
            throw new DoctorAlreadyExistsException("Doctor for user ID " + request.userId() + " already exists");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.userId()));
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with id: " + request.departmentId()));

        Doctor doctor = Doctor.builder()
                .user(user)
                .department(department)
                .licenseNumber(request.licenseNumber())
                .email(request.email())
                .phone(request.phone())
                .specialization(request.specialization())
                .experienceYears(request.experienceYears())
                .build();

        return mapToResponse(doctorRepository.save(doctor));
    }

    @Transactional
    public DoctorResponse updateDoctor(UUID id, DoctorRequest request) {
        Doctor doctor = findDoctorEntity(id);

        if (doctorRepository.existsByLicenseNumberAndIdNot(request.licenseNumber(), id)) {
            throw new DoctorAlreadyExistsException("Doctor with license number " + request.licenseNumber() + " already exists");
        }
        if (doctorRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DoctorAlreadyExistsException("Doctor with email " + request.email() + " already exists");
        }

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with id: " + request.departmentId()));

        doctor.setDepartment(department);
        doctor.setLicenseNumber(request.licenseNumber());
        doctor.setEmail(request.email());
        doctor.setPhone(request.phone());
        doctor.setSpecialization(request.specialization());
        doctor.setExperienceYears(request.experienceYears());

        return mapToResponse(doctorRepository.save(doctor));
    }

    @Transactional
    public void deleteDoctor(UUID id) {
        Doctor doctor = findDoctorEntity(id);
        doctorRepository.delete(doctor);
    }

    public Doctor findDoctorEntity(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with id: " + id));
    }

    private DoctorResponse mapToResponse(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getUser().getId(),
                doctor.getUser().getUsername(),
                doctor.getDepartment().getId(),
                doctor.getDepartment().getName(),
                doctor.getLicenseNumber(),
                doctor.getEmail(),
                doctor.getPhone(),
                doctor.getSpecialization(),
                doctor.getExperienceYears(),
                doctor.getCreatedAt(),
                doctor.getUpdatedAt()
        );
    }
}
