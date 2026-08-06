package com.portfolio.hms.dashboard.dto;

public record DashboardResponse(
        long totalDoctors,
        long totalPatients,
        long appointmentsToday,
        long activeDepartments,
        long pendingBills
) {}
