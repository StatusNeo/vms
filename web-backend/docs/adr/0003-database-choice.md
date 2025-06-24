# Architecture Decision Record

## Title
Use PostgreSQL as the primary database

## Status
Accepted

## Context
The application requires a reliable, open-source, and feature-rich relational database. PostgreSQL is well-supported, works well with Spring Data JPA, and is familiar to the team.

## Decision
We will use PostgreSQL as the primary database for all environments.

## Consequences
- Benefits from advanced features (JSONB, indexing, etc.)
- Easy integration with Spring Boot and TestContainers
- Requires PostgreSQL to be available in all environments

## Alternatives Considered
- MySQL: Also popular, but PostgreSQL offers more advanced features
- H2: Good for testing, but not suitable for production
- MongoDB: Not chosen due to strong relational data requirements 