package com.portfolio.hms.clinical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255)
        String name,

        String description
) {}
