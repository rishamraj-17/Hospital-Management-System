# 5. JWT Authentication

Date: 2026-08-05

## Status
Accepted

## Context
The Hospital Management System requires a secure, stateless way to authenticate users (Doctors, Receptionists, Admins) accessing the REST APIs. Managing session state on the server (via Cookies/Session IDs) is less scalable and not well-suited for modern stateless microservices or SPA frontends.

## Decision
We implemented JSON Web Tokens (JWT) for authentication. When a user logs in successfully, the backend issues an access token containing the user's roles and identity, which must be passed in the `Authorization: Bearer <token>` header of subsequent requests.

## Consequences
- **Statelessness**: The backend does not need to store session state, improving scalability and simplifying the architecture.
- **Flexibility**: Frontends (React/Angular) and mobile apps can easily consume the APIs.
- **Revocation Complexity**: JWTs cannot be easily revoked before expiration without implementing a token blacklist or shifting to a short-lived access token + long-lived refresh token strategy (planned for future phases).
