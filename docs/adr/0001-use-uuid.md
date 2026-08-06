# 1. Use UUIDs

Date: 2026-08-05

## Status
Accepted

## Context
Our Hospital Management System deals with sensitive information (patients, doctors, appointments). If we use standard sequential IDs (e.g., `1`, `2`, `3`), it becomes trivial for attackers to enumerate resources (e.g., `GET /api/v1/patients/1`, then `GET /api/v1/patients/2`) and guess system size or scrape data.

## Decision
We will use UUIDs (Universally Unique Identifiers) as primary keys across all database tables, specifically mapped with Hibernate's `GenerationType.UUID`.

## Consequences
- **Security**: It becomes computationally infeasible to enumerate resources or predict the next ID.
- **Distributed Generation**: IDs can be generated safely across different microservices without coordination if the system is ever split.
- **Performance**: UUIDs consume more space (16 bytes vs 8 bytes for Long) and can cause slight index fragmentation due to their random nature, though modern databases handle UUID indexing relatively well.
