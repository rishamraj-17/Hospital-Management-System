# 4. JPA Specification for Dynamic Search

Date: 2026-08-05

## Status
Accepted

## Context
Our application requires searching patients and doctors using multiple optional criteria (e.g., searching patients by name, blood group, or both). Writing a custom JPQL `@Query` for every permutation of optional parameters leads to combinatorial explosion and unmaintainable repository interfaces.

## Decision
We chose to use Spring Data JPA `Specification` to build dynamic, typesafe queries. This allows us to construct dynamic `WHERE` clauses programmatically.

## Consequences
- **Flexibility**: We can easily add new filter criteria without changing the repository interface.
- **Maintainability**: Avoids writing dozens of overloaded query methods.
- **Complexity**: Writing criteria builder logic is more verbose and harder to read than simple JPQL strings.
