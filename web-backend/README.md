# Visitor Management System for StatusNeo

# Functional flow
[Functional Design](docs/FunctionalDesign.md)

# Technical Design and Architecture
Check out the [docs](docs/) and [ADRs](docs/adr/README.md)

# VMS Development Guidelines

## Build and Configuration

### Prerequisites
- Java 21
- Maven
- Docker and Docker Compose (for running tests with TestContainers) — check https://java.testcontainers.org/supported_docker_environment/#overview 
  for appropriate Testcontainers settings in case using alternate Docker runtime. Follow exactly to avoid DB issues.
- PostgreSQL (for local development)

### Build Instructions
1. Clone the repository
2. Configure environment variables (see Configuration section)
3. Run the build:
   ```bash
   ./mvnw clean install
   ```

### Configuration
The project uses Spring Boot 3.4.5 with the following key configurations:

1. **Database**: PostgreSQL is used as the primary database
2. **Template Engine**: JTE (Java Template Engine) is used for HTML templates
3. **Authentication**: Azure AD integration via Spring Security OAuth2
4. **File Upload**: Apache POI for Excel file handling

Required environment variables:
- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- Azure AD configuration (see Azure AD section)

## Testing

### Test Structure
- Unit tests: `src/test/java`
- Test resources: `src/test/resources`
- Integration tests use TestContainers for database

### Running Tests
1. **All Tests**:
   ```bash
   ./mvnw test
   ```

2. **Specific Test Class**:
   ```bash
   ./mvnw test -Dtest=VisitorControllerTest
   ```

3. **Test with Coverage**:
   ```bash
   ./mvnw verify
   ```

### Writing Tests
1. **Unit Tests**:
   - Use JUnit 5
   - Follow naming convention: `{ClassName}Test`
   - Use `@SpringBootTest` for integration tests
   - Use `@Test` for individual test methods

2. **Integration Tests**:
   - Use TestContainers for database testing
   - Example:
   ```java
   @SpringBootTest
   @Testcontainers
   class VisitorServiceTest {
       @Container
       static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");
       
       @Test
       void shouldCreateVisitor() {
           // Test implementation
       }
   }
   ```

## Development Guidelines

### Code Style
1. **Java**:
   - Restrict Lombok usage unless absolutely essential
   - Follow Spring Boot best practices
   - Use constructor injection for dependencies
   - Document public APIs with JavaDoc

2. **Templates**:
   - JTE templates are in `src/main/jte`
   - Follow naming convention: `{feature}.jte`
   - Use proper HTML structure and semantic tags

### Project Structure
```
src/
├── main/
│   ├── java/          # Java source files
│   ├── jte/           # JTE templates
│   └── resources/     # Configuration files
└── test/
    ├── java/          # Test source files
    └── resources/     # Test configuration
```

### Key Dependencies
- Spring Boot 3.4.5
- Spring Data JPA
- Spring Security with OAuth2
- JTE 3.1.12
- Apache POI 5.2.5
- TestContainers

### Security Considerations
1. **Authentication**:
   - Azure AD integration is required
   - OAuth2 client configuration needed
   - Proper role-based access control implementation

2. **Data Protection**:
   - Use Jasypt for encrypting sensitive properties
   - Follow Spring Security best practices
   - Implement proper input validation

### Performance Guidelines
1. **Database**:
   - Use proper indexing
   - Implement pagination for list operations
   - Use JPA/Hibernate efficiently

2. **Templates**:
   - Use JTE's binary static content feature
   - Implement proper caching strategies
   - Optimize template rendering

### Deployment
1. **Containerization**:
   - Use provided Docker Compose for local development
   - Follow container best practices
   - Implement proper health checks

2. **CI/CD**:
   - GitHub Packages for artifact storage
   - Automated testing in CI pipeline
   - Proper versioning strategy

## Troubleshooting

### Common Issues
1. **Database Connection**:
   - Verify PostgreSQL is running
   - Verify Testcontainers is configured properly https://java.testcontainers.org/supported_docker_environment/#overview
     in case using alternate Docker runtime. Follow exactly to avoid DB issues.
   - Check connection properties
   - Ensure proper network access

2. **Template Issues**:
   - Clear JTE cache: `./mvnw clean`
   - Verify template syntax
   - Check template compilation

3. **Test Failures**:
   - Ensure Docker is running for TestContainers
   - Check test database configuration
   - Verify email server settings

### Debugging
1. **Application**:
   - Use Spring Boot DevTools
   - Enable debug logging
   - Use proper logging configuration

2. **Tests**:
   - Use `@TestPropertySource` for test-specific properties
   - Enable test logging
   - Use proper test data setup

## Additional Resources
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [JTE Documentation](https://jte.gg/docs/)
- [TestContainers Documentation](https://www.testcontainers.org/)
- [Azure AD Integration Guide](https://docs.microsoft.com/en-us/azure/active-directory/develop/quickstart-v2-spring-boot) 

### Infra
- VM with PostgreSQL installed
- StatusNeo O365 Oauth2 client integration
- StatusNeo O365 Outlook integration
- TLS public certificate for the domain

### Dev/test local infra
- Mock SMTP server
- Mock SMS server
- Local SSO server

### DevOps
Deploy with Github Action on EC2 from this repo
