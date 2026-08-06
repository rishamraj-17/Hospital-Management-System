# Hospital Management System (HMS)

## What is this?
The Hospital Management System (HMS) is an enterprise-grade backend REST API designed to manage hospital operations. It handles user authentication, doctor and patient management, an advanced appointment scheduling engine, prescription tracking, and billing cycles.

## Why was it built?
This project was built to demonstrate proficiency in modern backend engineering and system design. Rather than a simple CRUD wrapper, it enforces real-world business constraints (e.g., overlapping appointment detection, strict payment lifecycles, immutable records) using industry-standard patterns and robust database design.

## Features

- **JWT Authentication**: Secure, stateless authentication.
- **Role-Based Authorization**: Distinct access levels for `ADMIN`, `DOCTOR`, and `RECEPTIONIST`.
- **Doctor Management**: Department and specialization categorization.
- **Patient Management**: Secure records with automated Medical Record Number (MRN) generation.
- **Appointment Scheduling Engine**: Interval-overlap detection and strict state machine lifecycle.
- **Prescription Management**: Multi-item prescriptions linked to completed appointments.
- **Billing**: Full payment lifecycle from generation to refund.
- **Dashboard**: Admin analytics and aggregation endpoints.
- **Dynamic Search**: JPA Specifications for typesafe, dynamic querying.
- **Pagination**: Scalable list endpoints.
- **Optimistic Locking**: JPA `@Version` preventing concurrent modification anomalies.
- **Soft Deletes**: Hibernate `@SQLDelete` ensuring auditability and data preservation.
- **Flyway Migrations**: Version-controlled, reproducible database schemas.
- **Swagger/OpenAPI**: Auto-generated interactive API documentation.
- **Docker**: Fully containerized environment for seamless deployment.
- **GitHub Actions**: Automated CI pipeline running builds and tests.
- **Integration Testing**: Comprehensive test suite using Testcontainers and H2.

## How to run it?

### Prerequisites
- Docker & Docker Compose
- Java 21 (if running locally without Docker)

### Quick Start (Docker)
1. Clone the repository: `git clone https://github.com/yourusername/hms.git`
2. Copy the environment template: `cp .env.example .env`
3. Spin up the application and PostgreSQL database:
   ```bash
   docker compose up --build
   ```
4. Access the API documentation (Swagger) at: `http://localhost:8080/swagger-ui.html`

### Local Development
1. Start only the database: `docker compose up -d db`
2. Run the application: `./mvnw spring-boot:run`

## Technologies Used
- **Core**: Java 21, Spring Boot 3.3.x
- **Data**: PostgreSQL, Spring Data JPA, Hibernate, Flyway
- **Security**: Spring Security, JWT (Auth0), BCrypt
- **Observability**: Spring Boot Actuator, Micrometer
- **Testing**: JUnit 5, MockMvc, JaCoCo
- **Tooling**: Maven, Lombok, Docker, GitHub Actions

## Architecture

The system follows a layered, monolithic architecture (Controller -> Service -> Repository), heavily utilizing Spring Data JPA and Domain-Driven Design concepts for entities.

### Architecture Diagram
*(Imagine a diagram here showing Client -> Spring Security Filter Chain -> Controllers -> Services -> Repositories -> PostgreSQL)*

### ERD (Entity Relationship Diagram)
The database schema revolves around the `Patient`, `Doctor`, and `Appointment` triad.
*(Imagine an ERD here showing 1:N and 1:1 relationships between Departments, Doctors, Patients, Appointments, Prescriptions, and Bills)*

### Authentication Flow
1. Client POSTs credentials to `/api/v1/auth/login`.
2. Server validates via `AuthenticationManager`.
3. Server issues a signed JWT (`accessToken`).
4. Client passes JWT in `Authorization: Bearer <token>` for subsequent requests.

### Appointment Booking Sequence
1. Receptionist requests `POST /api/v1/appointments`.
2. `AppointmentService` validates doctor and patient existence.
3. `AppointmentService` queries DB for overlapping appointments using `startTime` and `durationMinutes`.
4. If no conflict, saves `Appointment` with `SCHEDULED` status.
5. If concurrent requests occur, `@Version` optimistic locking rejects the second transaction.

## Business Rules
- **Appointments**: Cannot overlap for the same doctor. Can only transition strictly (e.g., SCHEDULED -> CONFIRMED -> COMPLETED).
- **Billing**: Bills can only be generated for COMPLETED appointments. Amounts are immutable after payment. Paid bills cannot be cancelled, only refunded.
- **Deletion**: No records are ever truly deleted; they are marked `deleted = true` for audit preservation.

## API Overview
All endpoints are versioned under `/api/v1/`.

| Module | Base Path | Key Capabilities |
|--------|-----------|------------------|
| Auth | `/api/v1/auth` | Login, Register, Get Current User |
| Clinical | `/api/v1/departments`, `/api/v1/doctors` | Manage Departments, Doctors, Doctor Schedules |
| Patient | `/api/v1/patients` | Search, CRUD Patients |
| Scheduling | `/api/v1/appointments` | Book, Confirm, Complete, Cancel, Reschedule |
| Billing | `/api/v1/billing` | Generate Bills, Process Payments, Refunds |
| Dashboard | `/api/v1/dashboard/admin` | Aggregate Statistics |

## API Versioning Strategy
As the hospital grows, breaking changes to the API are inevitable. We strictly use URI versioning (`/api/v1/`). If a `v2` is introduced, `v1` will undergo a standard 6-month deprecation/sunset cycle using HTTP `Deprecation` headers.

## Architecture Decision Records (ADRs)
Our core architectural choices are explicitly documented in the `docs/adr/` directory:
- [0001 - Use UUID](docs/adr/0001-use-uuid.md)
- [0002 - Use Flyway](docs/adr/0002-use-flyway.md)
- [0003 - Soft Delete](docs/adr/0003-soft-delete.md)
- [0004 - JPA Specification](docs/adr/0004-jpa-specification.md)
- [0005 - JWT Authentication](docs/adr/0005-jwt-authentication.md)
