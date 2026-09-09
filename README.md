# Monitoring & Observability Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Production-style microservice monitoring and observability platform with Prometheus, Grafana, Alertmanager, and AI-powered incident intelligence.

## Overview

This project demonstrates a production-oriented monitoring and observability platform designed around a distributed microservice application.

The platform will progressively introduce application monitoring, metrics collection, centralized alerting, structured logging, incident analysis, and AI-assisted AIOps capabilities.

The system is designed around a clear separation of responsibilities:

* **Applications** generate business traffic, metrics, logs, and health information.
* **Observability components** collect and visualize operational data.
* **Alerting components** detect defined failure conditions.
* **AI components** analyze incident context and provide recommendations.
* **Deterministic automation** remains responsible for executing operational changes.

The AI layer is therefore designed as a **read-only decision-support system**, rather than an autonomous system that directly modifies infrastructure or application state.

## Architecture

The planned platform architecture is:

```text
                         ┌─────────────────────┐
                         │       Client        │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    API Gateway      │
                         │    Spring Boot      │
                         └───────┬─────┬───────┘
                                 │     │
                    ┌────────────┘     └────────────┐
                    ▼                               ▼
          ┌──────────────────┐             ┌──────────────────┐
          │   User Service   │             │   Order Service  │
          │    Spring Boot   │             │    Spring Boot   │
          └────────┬─────────┘             └────────┬─────────┘
                   │                                │
                   └───────────────┬────────────────┘
                                   ▼
                         ┌─────────────────────┐
                         │     PostgreSQL      │
                         └─────────────────────┘


Application Metrics
        │
        ▼
┌─────────────────────┐
│     Prometheus      │
└──────────┬──────────┘
           │
           ├──────────────► Grafana
           │
           └──────────────► Alertmanager
                                  │
                                  ▼
                         Incident Context Builder
                                  │
                                  ▼
                         AI Incident Analyzer
```

## Technology Stack

### Application

* Java 17
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* PostgreSQL
* Flyway

### Containerization

* Docker
* Docker Compose

### Observability

* Micrometer
* Prometheus
* Grafana
* Alertmanager
* Spring Boot Actuator

### AI / AIOps

* AI-powered incident analysis
* Alert correlation
* Anomaly detection
* Log analysis
* Incident summarization
* Root-cause analysis
* Remediation recommendations

## Project Structure

```text
monitoring-observability-platform/
│
├── gateway/
│
├── user-service/
│
├── order-service/
│
├── monitoring/
│   ├── prometheus/
│   ├── grafana/
│   └── alertmanager/
│
├── ai-incident-analyzer/
│
├── docker-compose.yml
├── .env.example
├── README.md
└── .gitignore
```

## Implemented Services

### User Service

The User Service is the first independently deployable business service in the platform.

It currently provides:

```text
POST   /users
GET    /users
GET    /users/{id}
DELETE /users/{id}
```

#### User Model

```text
id
name
email
createdAt
updatedAt
```

The service includes:

* REST API
* DTO-based request and response models
* Request validation
* PostgreSQL persistence
* Spring Data JPA
* Hibernate entity mapping
* Flyway database migrations
* Duplicate email protection
* Global exception handling
* Structured HTTP error responses
* Transaction management
* Actuator health endpoint

## Database

The platform currently uses PostgreSQL as its primary relational database.

The User Service uses Flyway for database schema management.

Current migration:

```text
V1__create_users_table.sql
```

Hibernate is configured with:

```text
ddl-auto: validate
```

This ensures that Hibernate validates the existing database schema rather than modifying it automatically.

## Health Checks

Spring Boot Actuator provides the initial health endpoint:

```text
GET /actuator/health
```

Example:

```json
{
  "status": "UP"
}
```

The health endpoint will later become part of the broader observability and service-health monitoring system.

## Error Handling

The User Service provides centralized exception handling for common API failures.

Examples include:

### Validation failure

```text
HTTP 400 Bad Request
```

### Duplicate user

```text
HTTP 409 Conflict
```

### User not found

```text
HTTP 404 Not Found
```

### Successful deletion

```text
HTTP 204 No Content
```

## Testing

The User Service includes integration-style API tests covering:

* Application context startup
* User creation
* User retrieval
* User listing
* User deletion
* Duplicate email handling
* Request validation
* Missing-user handling

The current test suite contains:

```text
8 tests
8 passed
0 failures
0 errors
```

Tests can be executed with:

```bash
cd user-service
./mvnw clean test
```

## Local Development

### Start PostgreSQL

From the project root:

```bash
docker compose up -d postgres
```

Verify the container:

```bash
docker ps
```

### Run User Service

Navigate to the User Service:

```bash
cd user-service
```

Run the application:

```bash
./mvnw spring-boot:run
```

The service starts on:

```text
http://localhost:8080
```

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Create a User

```bash
curl -i -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com"
  }'
```

### Get a User

```bash
curl -i http://localhost:8080/users/1
```

### Get All Users

```bash
curl -i http://localhost:8080/users
```

### Delete a User

```bash
curl -i -X DELETE http://localhost:8080/users/1
```

## Observability Roadmap

The platform will progressively add:

1. Application metrics
2. Prometheus metrics collection
3. Grafana dashboards
4. Alertmanager
5. Structured application logging
6. Incident context generation
7. AI-powered incident analysis
8. Alert correlation
9. Anomaly detection
10. AI log analysis
11. Incident timeline generation
12. Automated incident reports
13. Failure simulation
14. Incident recovery workflows
15. Incident history

## AI Incident Intelligence

The AI layer will analyze operational context collected from the platform.

Potential inputs include:

* Prometheus metrics
* Alertmanager alerts
* Application logs
* HTTP errors
* Service health
* Request latency
* Error rates
* Database health
* Container health
* Incident timelines

The analyzer will produce information such as:

```text
Incident
   ↓
Correlation
   ↓
Probable Root Cause
   ↓
Supporting Evidence
   ↓
Recommended Remediation
```

The AI system will **not directly execute remediation actions**.

Operational changes will remain under deterministic automation and explicit engineering control.

## Failure Engineering

A key feature of the project will be deliberate failure injection.

Example scenario:

```text
Database Failure
       ↓
Application Errors
       ↓
Metrics Change
       ↓
Prometheus Detection
       ↓
Alertmanager Alert
       ↓
Incident Context Builder
       ↓
AI Incident Analysis
       ↓
Root Cause + Remediation
       ↓
Database Recovery
       ↓
Service Recovery
```

This provides a realistic demonstration of an end-to-end observability and AIOps workflow rather than simply displaying dashboards.

## Project Status

**Stage 2 — User Service** ✅

Completed:

* Project initialization
* Spring Boot User Service
* PostgreSQL integration
* JPA / Hibernate persistence
* Flyway database migration
* User CRUD APIs
* DTO validation
* Duplicate email handling
* Global exception handling
* Actuator health check
* Automated tests
* Manual API verification

### Next Stage

**Stage 3 — Order Service** 🚧

The next stage will introduce the Order Service and establish the second business service in the distributed application.

## License

This project is licensed under the [MIT License](LICENSE).

