package com.portfolio.hms.patient.repository;

import com.portfolio.hms.patient.entity.Patient;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class PatientSpecification {

    public static Specification<Patient> search(String name, String bloodGroup) {
        return Specification.where(hasName(name)).and(hasBloodGroup(bloodGroup));
    }

    private static Specification<Patient> hasName(String name) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(name)) return null;
            String likePattern = "%" + name.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), likePattern),
                    cb.like(cb.lower(root.get("lastName")), likePattern)
            );
        };
    }

    private static Specification<Patient> hasBloodGroup(String bloodGroup) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(bloodGroup)) return null;
            return cb.equal(root.get("bloodGroup"), bloodGroup);
        };
    }
}
