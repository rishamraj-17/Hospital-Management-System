package com.portfolio.hms.clinical.service;

import com.portfolio.hms.clinical.dto.DepartmentRequest;
import com.portfolio.hms.clinical.dto.DepartmentResponse;
import com.portfolio.hms.clinical.entity.Department;
import com.portfolio.hms.clinical.repository.DepartmentRepository;
import com.portfolio.hms.exception.DepartmentAlreadyExistsException;
import com.portfolio.hms.exception.DepartmentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public Page<DepartmentResponse> getAllDepartments(Pageable pageable) {
        return departmentRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(UUID id) {
        Department department = findDepartmentEntity(id);
        return mapToResponse(department);
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.name())) {
            throw new DepartmentAlreadyExistsException("Department with name " + request.name() + " already exists");
        }

        Department department = Department.builder()
                .name(request.name())
                .description(request.description())
                .build();

        department = departmentRepository.save(department);
        return mapToResponse(department);
    }

    @Transactional
    public DepartmentResponse updateDepartment(UUID id, DepartmentRequest request) {
        Department department = findDepartmentEntity(id);

        if (departmentRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DepartmentAlreadyExistsException("Department with name " + request.name() + " already exists");
        }

        department.setName(request.name());
        department.setDescription(request.description());

        department = departmentRepository.save(department);
        return mapToResponse(department);
    }

    @Transactional
    public void deleteDepartment(UUID id) {
        Department department = findDepartmentEntity(id);
        departmentRepository.delete(department);
    }

    public Department findDepartmentEntity(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found with id: " + id));
    }

    private DepartmentResponse mapToResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }
}
