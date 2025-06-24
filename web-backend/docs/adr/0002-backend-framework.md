# Architecture Decision Record

## Title
Adopt Java Spring Boot as the backend framework

## Status
Accepted

## Context
We need a robust, scalable, and well-supported backend framework for the Visitor Management System (VMS). The team has experience with Java and Spring Boot, and it is widely used in the industry for building secure, maintainable, and production-grade applications.

## Decision
We will use Java Spring Boot (version 3.4.5) as the backend framework for this project.

## Consequences
- Leverages team expertise and community support
- Easy integration with Spring ecosystem (Security, Data JPA, etc.)
- Fast development with built-in tools and conventions
- Requires Java 21 as a prerequisite

## Alternatives Considered
- Node.js/Express: Less type safety, less alignment with team skills
- Python/Django: Good for rapid prototyping, but less preferred by the team
- .NET: Not as familiar to the team, licensing concerns 