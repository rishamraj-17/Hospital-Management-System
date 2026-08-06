# 3. Soft Delete

Date: 2026-08-05

## Status
Accepted

## Context
In healthcare systems, auditability and historical record preservation are critical. Hard deleting a doctor, patient, or appointment could corrupt historical billing records or medical histories.

## Decision
We will implement "Soft Delete" across all domain entities. This is achieved using Hibernate's `@SQLDelete(sql = "UPDATE table SET deleted = true WHERE id=?")` and `@SQLRestriction("deleted = false")`.

## Consequences
- **Data Preservation**: Data is never actually removed from the database, satisfying compliance and auditing requirements.
- **Simplicity**: Business logic and repositories do not need to manually filter out deleted rows; Hibernate handles it transparently via the `@SQLRestriction`.
- **Unique Constraints**: Unique constraints (like email or license number) require careful handling since deleted records still occupy those unique values.
- **Storage**: The database size will continuously grow over time as no data is permanently purged.
