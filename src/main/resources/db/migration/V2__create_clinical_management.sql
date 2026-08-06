CREATE TABLE departments (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    updated_by UUID,
    version BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE doctors (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    department_id UUID NOT NULL,
    license_number VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    specialization VARCHAR(255) NOT NULL,
    experience_years INT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    updated_by UUID,
    version BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_doctor_department FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE INDEX idx_doctors_specialization ON doctors(specialization);
CREATE INDEX idx_doctors_department ON doctors(department_id);

CREATE TABLE doctor_availability (
    id UUID PRIMARY KEY,
    doctor_id UUID NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_at TIMESTAMP(6) WITH TIME ZONE,
    updated_by UUID,
    version BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_doctor_availability_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

CREATE INDEX idx_doctor_availability_doctor ON doctor_availability(doctor_id);
