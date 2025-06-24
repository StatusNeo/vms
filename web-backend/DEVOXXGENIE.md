# DEVOXXGENIE.md

## Project Guidelines

### Build Commands

- **Build:** `./mvnw package`
- **Test:** `./mvnw test`
- **Single Test:** `./mvnw test --tests ClassName.methodName`
- **Clean:** `./mvnw clean`
- **Run:** `./mvnw spring-boot:run`

### Code Style

- **Formatting:** Use IDE or checkstyle for formatting
- **Naming:**
  - Use camelCase for variables, methods, and fields
  - Use PascalCase for classes and interfaces
  - Use SCREAMING_SNAKE_CASE for constants
- **Documentation:** Use JavaDoc for documentation
- **Imports:** Organize imports and avoid wildcard imports
- **Exception Handling:** Prefer specific exceptions and document throws

### Dependencies

The project uses the following main dependencies:

- **JUnit** - Testing framework

See pom.xml for the complete dependency list.

