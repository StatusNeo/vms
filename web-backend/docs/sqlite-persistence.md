# SQLite Persistence Layer

## Overview
This directory contains documentation for the optional SQLite persistence layer for the VMS application. SQLite is provided as an alternative to PostgreSQL for development, testing, and demonstration purposes.

## ⚠️ Important Notice
**SQLite is NOT intended for production use.** PostgreSQL with JPA remains the primary and recommended database for all production environments.

## When to Use SQLite

### Good Use Cases
- **Quick Local Development**: Testing features without PostgreSQL setup
- **Simple Demos**: Demonstrating functionality with minimal infrastructure
- **Unit/Integration Testing**: Faster tests without Docker overhead
- **Prototyping**: Rapid experimentation with new features

### Bad Use Cases
- **Production Deployments**: Never use SQLite in production
- **Performance Testing**: SQLite performance != PostgreSQL performance
- **Concurrent Access**: SQLite has limited concurrency support
- **PostgreSQL-Specific Features**: Testing JSONB, advanced indexes, etc.

## Enabling SQLite

### Prerequisites
SQLite is built into the dependencies. No additional installation required.

### Activation
SQLite requires **both** conditions to be met:

1. **Spring Profile**: `sqlite`
2. **Configuration Property**: `vms.persistence.sqlite.enabled=true`

### Option 1: Command Line
```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=sqlite \
  -Dspring-boot.run.arguments="--vms.persistence.sqlite.enabled=true"
```

### Option 2: Environment Variables
```bash
export SPRING_PROFILES_ACTIVE=sqlite
export VMS_PERSISTENCE_SQLITE_ENABLED=true
./mvnw spring-boot:run
```

### Option 3: IDE Configuration (IntelliJ IDEA)
1. Edit Run Configuration
2. Set **Active profiles**: `sqlite`
3. Add **VM options** or **Program arguments**: `--vms.persistence.sqlite.enabled=true`
4. Run the application

### Option 4: Application Properties (Not Recommended)
In `application.yml`:
```yaml
spring:
  profiles:
    active: sqlite
    
vms:
  persistence:
    sqlite:
      enabled: true
```

**Warning**: This permanently enables SQLite. Use command line or environment variables instead.

## Configuration Details

### Database File
- **Location**: `vms.db` in the working directory (where you run the application)
- **Auto-created**: The file is created automatically on first run
- **Reset**: Delete `vms.db` to reset the database

### Schema Management
- **Hibernate DDL**: `update` mode (auto-create/update tables)
- **Flyway**: Disabled (SQLite uses Hibernate DDL instead)
- **Migrations**: Not applied in SQLite mode

### Dialect
- **Hibernate Dialect**: `org.hibernate.community.dialect.SQLiteDialect`
- **JDBC Driver**: `org.sqlite.JDBC`

## Limitations

### Functional Limitations
1. **No Concurrent Writes**: SQLite locks the entire database for writes
2. **Limited ALTER TABLE**: Some schema changes require table recreation
3. **No Advanced Features**: No JSONB, advanced indexing, or PostgreSQL extensions
4. **Different SQL Dialect**: Some queries may behave differently

### Development Considerations
1. **Dialect Differences**: Code that works on SQLite might fail on PostgreSQL
2. **Performance**: SQLite performance is not representative of PostgreSQL
3. **Data Types**: Some type mappings differ between SQLite and PostgreSQL
4. **Transaction Behavior**: Different isolation and locking semantics

## Switching Back to PostgreSQL

### Option 1: Remove Profile
Simply run without the `sqlite` profile:
```bash
./mvnw spring-boot:run
```

### Option 2: Unset Environment Variables
```bash
unset SPRING_PROFILES_ACTIVE
unset VMS_PERSISTENCE_SQLITE_ENABLED
./mvnw spring-boot:run
```

### Verify PostgreSQL is Active
Check the logs on startup:
```
Hibernate: Using dialect: org.hibernate.dialect.PostgreSQLDialect
```

If you see `SQLiteDialect` instead, SQLite is still active.

## Testing with SQLite

### Unit Tests
Create a test configuration with SQLite profile:

```java
@SpringBootTest
@ActiveProfiles("sqlite")
@TestPropertySource(properties = {
    "vms.persistence.sqlite.enabled=true"
})
class MyServiceTest {
    // Tests here
}
```

### Integration Tests
For true integration testing, prefer TestContainers with PostgreSQL:
```java
@SpringBootTest
@Testcontainers
@Import(TestcontainersConfiguration.class)
class MyIntegrationTest {
    // Tests with actual PostgreSQL
}
```

## Troubleshooting

### SQLite Not Activating
**Problem**: Application still uses PostgreSQL

**Solutions**:
1. Verify both profile AND property are set
2. Check logs for: `Using dialect: SQLiteDialect`
3. Ensure no conflicting datasource configuration

### "Database Locked" Errors
**Problem**: `SQLiteException: database is locked`

**Solutions**:
1. Close other connections to `vms.db`
2. Stop other running instances of the application
3. Delete `vms.db` and restart (loses data)
4. SQLite may not be suitable for your concurrency needs

### Schema Issues
**Problem**: Tables not created or migrations failing

**Solutions**:
1. Verify Flyway is disabled (should be in `application-sqlite.yml`)
2. Check Hibernate DDL is set to `update`
3. Delete `vms.db` to recreate schema from scratch
4. SQLite has limited ALTER TABLE support - may need to recreate tables

### Switching Between Databases
**Problem**: Data not visible after switching

**Remember**:
- PostgreSQL data is in PostgreSQL (Docker/remote server)
- SQLite data is in `vms.db` file
- They are separate databases with separate data

## Architecture Decision Record
For the full rationale and decision details, see:
[ADR-0004: Optional SQLite Persistence](../docs/adr/0004-optional-sqlite-persistence.md)

## Support
SQLite support is provided as-is for development convenience. For production issues, always test against PostgreSQL first.
