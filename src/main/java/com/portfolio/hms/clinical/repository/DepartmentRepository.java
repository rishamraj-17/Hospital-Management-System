package com.portfolio.hms.clinical.repository;

import com.portfolio.hms.clinical.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID>, JpaSpecificationExecutor<Department> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
}
