# Agent Guidelines

Guidelines and instructions for AI agents working on this project.

## Technology Stack & Environment
- **Java Version:** Java 25.
- **Build Tool:** Apache Maven.
- **Lombok:** Use stable Lombok annotations where fitting (e.g., null checks via `@NonNull`).
- **Code Style:** Google Java Format.

## Architecture & Project Structure
- **Multi-Module Maven Setup:**
  - Submodules must inherit from the parent `pom.xml` of the main module.
  - Versions of dependencies used in more than one module must be maintained in the parent `pom.xml` (via `<dependencyManagement>`).
- **Design Principles:**
  - Prefer fluent and chained designs, both in implementation and when designing methods and interfaces.
  - Avoid big methods; apply separation of concerns architecture. Keep methods concise, modular, and focused.

## Documentation
- Write JavaDoc on all `public` and `protected` classes, interfaces, records, and methods.

## Testing & Verification
- **Unit Tests:** Always write or adapt unit tests whenever code is added or modified.
- **Test Execution:** Always ensure tests compile and pass before considering a change complete.

## Agent Constraints & Behavior
- **Scope Discipline:** Do not refactor unrelated code or alter existing public method signatures without explicit instruction.
