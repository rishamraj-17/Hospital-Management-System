# 2. Use Flyway for Database Migrations

Date: 2026-08-05

## Status
Accepted

## Context
We need a reliable and reproducible way to manage our database schema changes. Depending on Hibernate's `ddl-auto=update` in production can lead to destructive changes, inconsistent states across environments, and lack of version control over the database schema.

## Decision
We chose Flyway as our database migration tool. All schema changes must be written as versioned SQL scripts (e.g., `V1__create_users.sql`, `V2__create_clinical.sql`).

## Consequences
- **Version Control**: Every database change is tracked in Git alongside the application code.
- **Reproducibility**: Spinning up a fresh database in any environment is guaranteed to produce the exact same schema.
- **Overhead**: Developers must manually write SQL scripts instead of relying purely on JPA annotations for schema generation.
