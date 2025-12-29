# IMES - Intern Management & Evaluation System

## Overview
IMES (Intern Management & Evaluation System) is an enterprise-grade platform for managing internship programs, tracking performance, and conducting evaluations.

## Architecture
Multi-module Spring Boot application with clean architecture principles:

- **api**: REST controllers, API DTOs, request/response mappers
- **core**: Business logic, domain models, service interfaces
- **infra**: Data persistence, repositories, external integrations
- **common**: Shared utilities, constants, common DTOs

## Tech Stack
- Java 21
- Spring Boot 3.2.2
- PostgreSQL
- Liquibase (database migrations)
- JWT Authentication
- Lombok & MapStruct
- Swagger/OpenAPI

## Prerequisites
- Java 21 or higher
- PostgreSQL 15 or higher
- Gradle 8.x

## Getting Started

### 1. Clone repository
```bash
git clone <repository-url>
cd imes-service
```

### 2. Setup database
```bash
createdb imes_db
```

### 3. Configure application
Copy `api/src/main/resources/application.example.yml` to `application-local.yml` and update database credentials.

### 4. Build project
```bash
./gradlew clean build
```

### 5. Run application
```bash
./gradlew :api:bootRun
```

Application will start on `http://localhost:8080`

## API Documentation
Swagger UI: `http://localhost:8080/swagger-ui.html`

## Project Status
🚧 Under active development

## License
Proprietary - FPT University Graduation Thesis Project
