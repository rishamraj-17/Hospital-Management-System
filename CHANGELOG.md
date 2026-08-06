# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-08-05

### Added
- **Authentication**: JWT-based secure authentication with Role-based authorization (`ADMIN`, `DOCTOR`, `RECEPTIONIST`).
- **Clinical Management**: Endpoints for Departments and Doctors.
- **Patient Management**: Secure patient record management with dynamic searching and Medical Record Number (MRN) auto-generation.
- **Appointment Scheduling Engine**: Robust interval-overlap detection algorithm with Optimistic Locking (`@Version`) preventing simultaneous bookings.
- **Prescription Management**: Ability for Doctors to write multi-item prescriptions for completed appointments.
- **Billing Module**: Automated invoice generation for completed appointments and complete payment lifecycle (Pending -> Paid -> Refunded/Cancelled).
- **Dashboard**: Aggregation endpoints for hospital statistics.
- **Database & Architecture**: Fully containerized PostgreSQL, managed by Flyway migrations. Implemented Soft Deletes globally.
- **Testing**: Comprehensive integration testing using H2 and Spring Boot Test, with JaCoCo measuring code coverage.
- **CI/CD**: Fully configured GitHub Actions workflow (`maven.yml`) to ensure continuous integration.
- **Documentation**: Swagger OpenAPI integration, complete Architecture Decision Records (ADRs) under `docs/adr/`.
