# Architecture Decision Record

## Title
Use TestContainers for integration testing

## Status
Accepted

## Context
Integration tests require real database and email server instances. TestContainers allows running lightweight, disposable containers for testing, ensuring consistency across environments.

## Decision
We will use TestContainers for integration testing of the database and email server.

## Consequences
- Reliable, reproducible integration tests
- Requires Docker for running tests
- Adds some complexity to test setup

## Alternatives Considered
- Embedded databases: Not production-like
- Manually managed test servers: Harder to automate and maintain 