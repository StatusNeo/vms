# Architecture Decision Record

## Title
Add Optional SQLite Persistence Layer

## Status
Accepted

## Context
While PostgreSQL with JPA is the primary production database, there are scenarios where developers and testers may benefit from a lightweight, file-based database option:

1. **Quick Local Development**: Developers can run the application without setting up PostgreSQL
2. **Simplified Testing**: Integration tests can use SQLite without Docker/TestContainers overhead
3. **Demos and Prototyping**: Easier to share and demonstrate features without infrastructure setup
4. **CI/CD Optimization**: Faster builds in certain scenarios

However, SQLite has limitations:
- Limited concurrency support
- No advanced PostgreSQL-specific features (JSONB, advanced indexing)
- Different SQL dialect quirks
- Not suitable for production workloads

## Decision
We will add an optional SQLite persistence layer that:

1. **Remains Disabled by Default**: PostgreSQL/JPA remains the default configuration
2. **Profile-Based Activation**: SQLite is enabled via Spring profile (`sqlite`) AND a specific property (`vms.persistence.sqlite.enabled=true`)
3. **Maintains JPA Compatibility**: Uses the same JPA entities and repositories
4. **Leverages Hibernate Community Dialects**: Uses `org.hibernate.community.dialect.SQLiteDialect`
5. **Disables Flyway**: SQLite relies on Hibernate's DDL auto-generation instead of migrations

## Implementation Details

### Dependencies
- `sqlite-jdbc` (3.47.2.0): JDBC driver for SQLite
- `hibernate-community-dialects`: Provides SQLite dialect for Hibernate

### Configuration
- Profile: `sqlite` in `application-sqlite.yml`
- Property gate: `vms.persistence.sqlite.enabled=true`
- Database file: `vms.db` (created in working directory)
- Hibernate DDL: `update` mode (auto-create/update schema)

### Activation
To use SQLite, both conditions must be met:
1. Profile: `--spring.profiles.active=sqlite`
2. Property: `vms.persistence.sqlite.enabled=true`

Example:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=sqlite -Dspring-boot.run.arguments="--vms.persistence.sqlite.enabled=true"
```

## Consequences

### Positive
- Developers can quickly test without PostgreSQL setup
- Simpler demo and prototyping scenarios
- Potential CI/CD time savings for certain test scenarios
- Maintains full JPA compatibility (same entities and repositories work)

### Negative
- Additional dependency to maintain
- SQLite behavior differences may mask PostgreSQL-specific issues
- Developers might accidentally develop against SQLite instead of production DB
- Requires explicit double opt-in to prevent accidental usage

### Neutral
- Configuration complexity slightly increased
- Need to ensure production deployments never accidentally enable SQLite

## Alternatives Considered

### H2 Database
- **Pro**: More commonly used in Spring Boot projects, better PostgreSQL compatibility mode
- **Con**: Heavier weight than SQLite, still has dialect differences

### TestContainers Only
- **Pro**: Exact PostgreSQL match, no dialect issues
- **Con**: Requires Docker, slower startup, more complex setup

### Keep PostgreSQL Only
- **Pro**: Single database to support, no dialect confusion
- **Con**: Higher barrier to entry for new developers, slower local development setup

## Notes
- SQLite is **NOT** for production use
- All CI/CD production tests should continue using PostgreSQL via TestContainers
- This option is primarily for developer convenience and specific testing scenarios
- Production configuration remains unchanged (PostgreSQL/JPA)
