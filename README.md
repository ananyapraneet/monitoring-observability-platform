# Monitoring & Observability Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Production-style microservice monitoring and observability platform with Prometheus, Grafana, Alertmanager, and AI-powered incident intelligence.

## Overview

This project demonstrates a production-oriented monitoring and observability platform designed around a distributed microservice application.

The platform progressively introduces application services, database persistence, health monitoring, metrics collection, centralized alerting, structured logging, incident analysis, and AI-assisted AIOps capabilities.

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
          │     :8080        │             │     :8081        │
          └────────┬─────────┘             └────────┬─────────┘
                   │                                │
                   ▼                                ▼
          ┌──────────────────┐             ┌──────────────────┐
          │    PostgreSQL    │             │    PostgreSQL    │
          │  public schema   │             │ order_service    │
          └──────────────────┘             │     schema       │
                                           └──────────────────┘


                    Application Metrics / Logs / Health
                              │
                              ▼
                    ┌─────────────────────┐
                    │     Observability    │
                    │       Pipeline       │
                    └──────────┬──────────┘
                               │
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
       ┌─────────────┐  ┌─────────────┐  ┌──────────────┐
       │ Prometheus  │  │   Grafana   │  │ Alertmanager │
       └─────────────┘  └─────────────┘  └──────┬───────┘
                                                │
                                                ▼
                                    Incident Context Builder
                                                │
                                                ▼
                                      AI Incident Analyzer
```

The architecture will evolve as additional observability and AIOps stages are implemented.

## Technology Stack

### Application

* Java 17
* Spring Boot 4.1.1
* Spring Web
* Spring Data JPA
* Hibernate
* PostgreSQL 17
* Flyway

### Containerization

* Docker
* Docker Compose

### Observability

* Spring Boot Actuator
* Micrometer
* Prometheus
* Grafana
* Alertmanager

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

The User Service runs on:

```text
http://localhost:8080
```

### Order Service

The Order Service is the second independently deployable business service in the platform.

It currently provides:

```text
POST   /orders
GET    /orders
GET    /orders/{id}
PUT    /orders/{id}
DELETE /orders/{id}
```

#### Order Model

```text
id
userId
product
quantity
amount
status
createdAt
updatedAt
```

#### Order Status

Orders support the following lifecycle states:

```text
CREATED
PROCESSING
COMPLETED
CANCELLED
```

The service includes:

* REST API
* DTO-based request and response models
* Request validation
* PostgreSQL persistence
* Spring Data JPA
* Hibernate entity mapping
* Flyway database migrations
* Dedicated database schema
* Global exception handling
* Structured HTTP error responses
* Transaction management
* Application-level logging
* Actuator health endpoint

The Order Service runs on:

```text
http://localhost:8081
```

### Service Independence

The platform currently contains two independently deployable Spring Boot services:

```text
                    PostgreSQL
                        │
              ┌─────────┴─────────┐
              │                   │
              ▼                   ▼
        User Service         Order Service
          :8080                  :8081
              │                   │
              ▼                   ▼
        public schema       order_service schema
```

Both services use the same PostgreSQL instance during local development while maintaining **separate database schemas and Flyway migration histories**.

This provides service-level schema isolation while keeping the local development environment lightweight.

## Database

The platform currently uses PostgreSQL as its primary relational database.

PostgreSQL is started through Docker Compose:

```bash
docker compose up -d postgres
```

### User Service Schema

The User Service currently uses the PostgreSQL `public` schema.

Its initial migration is:

```text
V1__create_users_table.sql
```

The migration creates the `users` table with:

```text
id
name
email
created_at
updated_at
```

The email column is protected by a unique constraint.

### Order Service Schema

The Order Service uses a dedicated:

```text
order_service
```

schema.

Its initial migration is:

```text
V1__create_orders_table.sql
```

The migration creates the `orders` table with:

```text
id
user_id
product
quantity
amount
status
created_at
updated_at
```

The Order Service is configured with:

```text
ddl-auto: validate
```

Hibernate therefore validates the existing schema rather than modifying it automatically.

Flyway remains responsible for database schema evolution.

## Health Checks

Spring Boot Actuator provides health endpoints for both services.

### User Service

```text
GET http://localhost:8080/actuator/health
```

### Order Service

```text
GET http://localhost:8081/actuator/health
```

Example response:

```json
{
  "status": "UP"
}
```

These health endpoints will later become part of the broader service-health monitoring and failure-detection system.

## Error Handling

Both business services use centralized exception handling for common API failures.

### Validation Failure

```text
HTTP 400 Bad Request
```

Example validation scenarios include:

* Missing required fields
* Blank product or name values
* Invalid quantity
* Invalid amount
* Invalid email

### Resource Not Found

```text
HTTP 404 Not Found
```

### Duplicate User

The User Service returns:

```text
HTTP 409 Conflict
```

when attempting to create a user with an existing email address.

### Successful Deletion

Successful deletion returns:

```text
HTTP 204 No Content
```

## Application Logging

The Order Service currently includes application-level logging using SLF4J.

Examples of logged business events include:

```text
Created order with id=...
Updated order with id=... to status=...
Deleted order with id=...
```

These logs establish the foundation for the structured logging and centralized incident-analysis pipeline that will be introduced in later observability stages.

Full JSON-based structured logging is intentionally deferred to the dedicated logging stage.

## Testing

The Order Service contains unit, web-layer, and application-context tests.

Current test coverage includes:

* Application context startup
* Order creation
* Order retrieval
* Order listing
* Order update
* Order deletion
* Missing-order handling
* Request validation
* Controller HTTP status codes
* Controller request/response behavior
* Service-layer repository interactions

The current Order Service test suite contains:

```text
15 tests
15 passed
0 failures
0 errors
```

Run the complete test suite with:

```bash
cd order-service
./mvnw clean test
```

The User Service currently contains:

```text
8 tests
8 passed
0 failures
0 errors
```

Run the User Service tests with:

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

The PostgreSQL container is exposed locally on:

```text
localhost:5432
```

### Run User Service

Navigate to the User Service:

```bash
cd user-service
```

Run:

```bash
./mvnw spring-boot:run
```

The service starts on:

```text
http://localhost:8080
```

### Run Order Service

Open another terminal and navigate to:

```bash
cd order-service
```

Run:

```bash
./mvnw spring-boot:run
```

The service starts on:

```text
http://localhost:8081
```

Both services can therefore run simultaneously during local development.

## User Service API Examples

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

## Order Service API Examples

### Health Check

```bash
curl http://localhost:8081/actuator/health
```

### Create an Order

```bash
curl -i -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "product": "Mechanical Keyboard",
    "quantity": 1,
    "amount": 129.99
  }'
```

### Get an Order

```bash
curl -i http://localhost:8081/orders/1
```

### Get All Orders

```bash
curl -i http://localhost:8081/orders
```

### Update an Order

```bash
curl -i -X PUT http://localhost:8081/orders/1 \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "product": "Mechanical Keyboard",
    "quantity": 2,
    "amount": 259.98,
    "status": "PROCESSING"
  }'
```

### Delete an Order

```bash
curl -i -X DELETE http://localhost:8081/orders/1
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

**Stage 3 — Order Service** ✅

Completed stages:

### Stage 1 — Project Initialization ✅

* Project structure
* Spring Boot project foundation
* Docker Compose PostgreSQL environment
* Environment configuration
* Initial documentation

### Stage 2 — User Service ✅

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

### Stage 3 — Order Service ✅

* Independent Spring Boot Order Service
* Order CRUD APIs
* Order lifecycle statuses
* DTO validation
* PostgreSQL persistence
* Dedicated `order_service` database schema
* Flyway database migration
* Hibernate schema validation
* Global exception handling
* Actuator health check
* Application-level logging
* Service-layer unit tests
* Controller-layer tests
* Full application-context test
* Manual API verification
* 15/15 automated tests passing

### Current Architecture

The application layer now consists of:

```text
Client
  │
  ├──────────────► User Service (:8080)
  │
  └──────────────► Order Service (:8081)
                         │
                         ▼
                    PostgreSQL
```

The API Gateway will be introduced in the next stage to provide a single entry point for these services.

### Next Stage

**Stage 4 — API Gateway** 🚧

The next stage will introduce a Spring Boot API Gateway that provides a unified entry point for the User Service and Order Service.

Planned responsibilities include:

* Single client-facing entry point
* Request routing
* Service discovery / routing configuration
* Request forwarding
* Gateway-level health monitoring
* Foundation for future distributed request observability

## License

This project is licensed under the [MIT License](LICENSE).

