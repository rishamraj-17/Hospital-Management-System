package com.portfolio.hms.clinical.entity;

import com.portfolio.hms.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE departments SET deleted = true WHERE id=? and version=?")
@SQLRestriction("deleted = false")
public class Department extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;
}
