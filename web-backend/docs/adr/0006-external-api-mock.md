# Architecture Decision Record

## Title
Use WireMock for mocking external HTTP services in tests

## Status
Accepted

## Context
We need a reliable way to simulate external HTTP APIs (like MS Graph) for isolated testing.

## Decision
Adopt WireMock with its official Spring Boot integration for mocking HTTP services during tests.

## Consequences
- We need to create and maintain stub mappings for the endpoint
- In dev/test, no real email will be sent, but business logic will be tested properly
- Developers can test mail logic without needing Azure or Graph tokens
- Unit and integration tests become faster and more reliable


## Alternatives Considered
### 1. WireMock (Selected)
**Description**: Simulates HTTP responses without Docker.

- **Pros**:
    - Fast and lightweight
    - Easy to stub and modify responses
    - No Docker dependency

- **Cons**:
    - Stubs require manual updates
    - Limited simulation of real API behavior (e.g., auth, headers)

---

### 2. Testcontainers + MailHog
**Description**: Uses a Docker-based SMTP server for realistic email testing.

- **Pros**:
    - Closer to real email behavior
    - Fully isolated and reproducible environments

- **Cons**:
    - Requires Docker
    - Slower test startup
    - More complex setup (networking, ports)

---

### 3. Spring Cloud Contract
**Description**: Contract-based testing with auto-generated WireMock stubs.

- **Pros**:
    - Ensures consistency between consumer and producer
    - Auto-generates mocks and tests from contracts

- **Cons**:
    - Higher learning curve
    - Requires shared contracts and coordination
    - Overkill for third-party APIs like Microsoft Graph