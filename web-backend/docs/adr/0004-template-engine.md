# Architecture Decision Record

## Title
Use JTE as the template engine

## Status
Accepted

## Context
The project requires a fast, type-safe, and secure server-side template engine for rendering HTML. JTE is modern, integrates well with Java, and offers compile-time safety.

## Decision
We will use JTE (Java Template Engine) for all server-side HTML rendering.

## Consequences
- Type-safe templates with compile-time checks
- Fast rendering and low memory usage
- Requires learning JTE syntax and best practices

## Alternatives Considered
- Thymeleaf: More popular, but less performant and less type-safe
- JSP: Outdated and less secure
- Freemarker: Flexible, but less type-safe 