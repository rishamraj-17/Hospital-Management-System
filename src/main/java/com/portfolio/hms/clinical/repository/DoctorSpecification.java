package com.portfolio.hms.clinical.repository;

import com.portfolio.hms.clinical.entity.Doctor;
import org.springframework.data.jpa.domain.Specification;

public class DoctorSpecification {

    public static Specification<Doctor> hasSpecialization(String specialization) {
        return (root, query, cb) -> specialization == null ? null : cb.equal(cb.lower(root.get("specialization")), specialization.toLowerCase());
    }

    public static Specification<Doctor> hasDepartmentName(String departmentName) {
        return (root, query, cb) -> departmentName == null ? null : cb.equal(cb.lower(root.join("department").get("name")), departmentName.toLowerCase());
    }
}
