# Monitoring & Observability Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Production-style microservice monitoring and observability platform with Prometheus, Grafana, Alertmanager, and AI-powered incident intelligence.

## Overview

This project demonstrates a production-oriented monitoring and observability platform designed around a distributed microservice application.

The platform progressively introduces application services, database persistence, API gateway routing, containerization, health monitoring, metrics collection, centralized alerting, structured logging, incident analysis, and AI-assisted AIOps capabilities.

The system is designed around a clear separation of responsibilities:

* **Applications** generate business traffic, metrics, logs, and health information.
* **API Gateway** provides a centralized client-facing entry point and propagates distributed request context.
* **Container Platform** provides reproducible local deployment, service networking, health checks, and persistent database storage.
* **Observability components** collect and visualize operational data.
* **Alerting components** detect defined failure conditions.
* **AI components** analyze incident context and provide recommendations.
* **Deterministic automation** remains responsible for executing operational changes.

The AI layer is therefore designed as a **read-only decision-support system**, rather than an autonomous system that directly modifies infrastructure or application state.

## Architecture

The current application architecture is:

```text
                         ┌─────────────────────┐
                         │       Client        │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    API Gateway      │
                         │    Spring Boot      │
                         │       :8082         │
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
                   └──────────────┬─────────────────┘
                                  │
                                  ▼
                         ┌──────────────────┐
                         │    PostgreSQL    │
                         │      :5432       │
                         └────────┬─────────┘
                                  │
                                  ▼
                         ┌──────────────────┐
                         │ Persistent Docker│
                         │      Volume      │
                         └──────────────────┘


                    Application Metrics / Logs / Health
                              │
                              ▼
                    ┌─────────────────────┐
                    │     Observability   │
                    │       Pipeline      │
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

The API Gateway currently provides the single client-facing entry point for the business services.

The complete application stack can now be started locally through Docker Compose.

The observability and AIOps layers will be progressively connected to the application traffic as later stages are implemented.

## Technology Stack

### Application

* Java 17
* Spring Boot 4.1.1
* Spring Web
* Spring Data JPA
* Hibernate
* PostgreSQL 17
* Flyway
* Spring Boot Actuator

### API Gateway

* Spring Boot
* Spring Web
* Spring `RestClient`
* Servlet Filters
* Correlation ID propagation
* Centralized downstream error handling

### Containerization

* Docker
* Docker Compose
* Multi-stage Docker builds
* Non-root application containers
* Docker health checks
* Docker bridge networking
* Persistent Docker volumes

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
│   ├── .mvn/
│   │   └── wrapper/
│   │       └── maven-wrapper.properties
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/ananyapraneet/monitoring/gateway/
│   │   │   │       ├── config/
│   │   │   │       │   └── RestClientConfig.java
│   │   │   │       ├── controller/
│   │   │   │       │   ├── OrderGatewayController.java
│   │   │   │       │   └── UserGatewayController.java
│   │   │   │       ├── exception/
│   │   │   │       │   └── GatewayExceptionHandler.java
│   │   │   │       ├── filter/
│   │   │   │       │   ├── CorrelationIdFilter.java
│   │   │   │       │   └── RequestLoggingFilter.java
│   │   │   │       └── GatewayApplication.java
│   │   │   └── resources/
│   │   │       └── application.yaml
│   │   ├── test/
│   │   │   └── java/
│   │   │       └── com/ananyapraneet/monitoring/gateway/
│   │   │           └── GatewayApplicationTests.java
│   │   ├── pom.xml
│   │   ├── mvnw
│   │   ├── mvnw.cmd
│   │   └── HELP.md
│   │
├── user-service/
│   ├── .mvn/
│   │   └── wrapper/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/ananyapraneet/monitoring/userservice/
│   │   │   │       ├── controller/
│   │   │   │       ├── dto/
│   │   │   │       ├── entity/
│   │   │   │       ├── exception/
│   │   │   │       ├── repository/
│   │   │   │       ├── service/
│   │   │   │       └── UserServiceApplication.java
│   │   │   └── resources/
│   │   │       ├── db/
│   │   │       │   └── migration/
│   │   │       │       └── V1__create_users_table.sql
│   │   │       └── application.yml
│   │   ├── test/
│   │   │   └── java/
│   │   │       └── com/ananyapraneet/monitoring/userservice/
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   ├── mvnw
│   │   ├── mvnw.cmd
│   │   └── HELP.md
│   │
├── order-service/
│   ├── .mvn/
│   │   └── wrapper/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/ananyapraneet/monitoring/orderservice/
│   │   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       └── OrderServiceApplication.java
│   │   │   └── resources/
│   │   │       ├── db/
│   │   │       │   └── migration/
│   │   │       │       └── V1__create_orders_table.sql
│   │   │       └── application.yaml
│   │   ├── test/
│   │   │   └── java/
│   │   │       └── com/ananyapraneet/monitoring/orderservice/
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   ├── mvnw
│   │   ├── mvnw.cmd
│   │   └── HELP.md
│   │
├── monitoring/
│   ├── prometheus/
│   ├── grafana/
│   └── alertmanager/
│
├── ai-incident-analyzer/
│
├── gateway/
│
├── docker-compose.yml
├── .env.example
├── README.md
└── .gitignore
```

The detailed service structure will continue to evolve as additional stages introduce observability, logging, and AIOps components.

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
* Docker containerization
* Container health check

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
* Docker containerization
* Container health check

The Order Service runs on:

```text
http://localhost:8081
```

### API Gateway

The API Gateway provides a centralized entry point for clients accessing the business services.

The Gateway runs on:

```text
http://localhost:8082
```

#### User Routes

```text
POST   /api/users
GET    /api/users
GET    /api/users/{id}
DELETE /api/users/{id}
```

These routes are forwarded to the User Service:

```text
/api/users/* → http://localhost:8080/users/*
```

#### Order Routes

```text
POST   /api/orders
GET    /api/orders
GET    /api/orders/{id}
PUT    /api/orders/{id}
DELETE /api/orders/{id}
```

These routes are forwarded to the Order Service:

```text
/api/orders/* → http://localhost:8081/orders/*
```

The Gateway currently provides:

* Centralized client-facing entry point
* Request routing
* Request forwarding
* Correlation ID generation
* Correlation ID preservation
* Correlation ID propagation
* Basic request logging
* Downstream HTTP error propagation
* Gateway health endpoint
* Configurable downstream service URLs
* Docker containerization
* Container health check

## Correlation IDs

The Gateway uses the:

```text
X-Correlation-ID
```

HTTP header to associate requests across the distributed application.

If a client provides a correlation ID, the Gateway preserves it.

If no correlation ID is provided, the Gateway generates a UUID.

The correlation ID is also forwarded to downstream services through the Gateway's `RestClient` configuration.

Example:

```text
Client
  │
  │ X-Correlation-ID: abc-123
  ▼
API Gateway
  │
  │ X-Correlation-ID: abc-123
  ▼
Order Service
```

This establishes the foundation for distributed request observability and structured logging in later stages.

## Gateway Request Logging

The Gateway currently performs basic request-completion logging.

Example:

```text
Request completed: method=GET uri=/api/orders/2 status=200 correlationId=test-correlation-789
```

The request logging filter records:

* HTTP method
* Request URI
* HTTP response status
* Correlation ID

More comprehensive structured application logging will be introduced during the dedicated logging stage.

## Gateway Error Handling

The Gateway propagates HTTP errors returned by downstream services.

For example, if the Order Service returns:

```text
HTTP 404 Not Found
```

for a missing order, the Gateway preserves the downstream status and response body.

Example:

```text
Client
   │
   ▼
API Gateway
   │
   ▼
Order Service
   │
   └── 404 Not Found
           │
           ▼
      API Gateway
           │
           ▼
        Client
```

This prevents downstream application errors from being converted into generic Gateway `500 Internal Server Error` responses.

## Service Independence

The platform currently contains two independently deployable Spring Boot business services and one API Gateway:

```text
                         API Gateway
                            :8082
                              │
                    ┌─────────┴─────────┐
                    │                   │
                    ▼                   ▼
              User Service        Order Service
                 :8080                 :8081
                    │                   │
                    ▼                   ▼
              public schema       order_service schema
                    │                   │
                    └─────────┬─────────┘
                              ▼
                         PostgreSQL
                            :5432
```

Both business services use the same PostgreSQL instance during local development while maintaining **separate database schemas and Flyway migration histories**.

This provides service-level schema isolation while keeping the local development environment lightweight.

## Dockerized Local Platform

The complete application stack can now be started through Docker Compose.

The Dockerized platform consists of:

```text
┌────────────────────────────────────────────────────┐
│              Docker Compose Platform               │
│                                                    │
│  ┌──────────────┐                                  │
│  │ API Gateway  │ :8082                            │
│  └──────┬───────┘                                  │
│         │                                           │
│    ┌────┴────┐                                      │
│    ▼         ▼                                      │
│ ┌───────┐ ┌───────────────┐                         │
│ │ User  │ │    Order      │                         │
│ │ :8080 │ │    :8081      │                         │
│ └───┬───┘ └───────┬───────┘                         │
│     │             │                                 │
│     └──────┬──────┘                                 │
│            ▼                                        │
│     ┌──────────────┐                                │
│     │  PostgreSQL  │ :5432                          │
│     └──────┬───────┘                                │
│            │                                        │
│            ▼                                        │
│     postgres-data                                   │
│     persistent volume                               │
└────────────────────────────────────────────────────┘
```

### Docker Compose Services

The Compose platform currently contains:

```text
postgres
user-service
order-service
gateway
```

All application services communicate through the dedicated Docker bridge network:

```text
monitoring-network
```

Docker service names are used for internal communication.

For example:

```text
Gateway → http://user-service:8080
Gateway → http://order-service:8081

User Service → postgres:5432
Order Service → postgres:5432
```

This avoids relying on host-local addresses between containers.

### Start the Complete Platform

From the project root:

```bash
docker compose up --build
```

The command builds the application images and starts:

```text
PostgreSQL
User Service
Order Service
API Gateway
```

### Verify Container Health

Run:

```bash
docker compose ps
```

All four containers should report:

```text
healthy
```

Expected services:

```text
monitoring-postgres
monitoring-user-service
monitoring-order-service
monitoring-api-gateway
```

### Container Health Checks

The Compose configuration includes health checks for all services.

PostgreSQL uses:

```text
pg_isready
```

Application services use their Actuator health endpoints:

```text
/actuator/health
```

The Gateway depends on the User Service and Order Service becoming healthy before starting.

The User Service and Order Service depend on PostgreSQL becoming healthy.

This establishes the following startup dependency chain:

```text
PostgreSQL
    │
    ├──────────────► User Service
    │
    └──────────────► Order Service
                           │
                           ▼
                     API Gateway
```

### Non-Root Containers

The application containers run using a dedicated non-root Linux user:

```text
appuser
UID: 10001
```

The Dockerfiles use multi-stage builds so that Maven build tooling remains in the builder stage while the final runtime image contains only the Java runtime and application artifact.

The runtime containers use:

```text
eclipse-temurin:17-jre
```

This reduces the final runtime image footprint and avoids running the application as root.

### Persistent PostgreSQL Storage

PostgreSQL uses a named Docker volume:

```text
monitoring-observability-platform_postgres-data
```

The volume is mounted to:

```text
/var/lib/postgresql/data
```

This ensures PostgreSQL data survives container recreation and restart.

Persistence was verified by restarting the PostgreSQL container and successfully retrieving previously stored application data afterward.

### Docker Network

All services are attached to:

```text
monitoring-network
```

The network allows containers to communicate using Compose service names rather than host-specific addresses.

This produces a reproducible local environment that closely resembles a multi-service deployment topology.

## Database

The platform currently uses PostgreSQL as its primary relational database.

PostgreSQL is started through Docker Compose:

```bash
docker compose up -d postgres
```

Or as part of the complete application platform:

```bash
docker compose up --build
```

The PostgreSQL container is exposed locally on:

```text
localhost:5432
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

Spring Boot Actuator provides health endpoints for the services and Gateway.

### User Service

```text
GET http://localhost:8080/actuator/health
```

### Order Service

```text
GET http://localhost:8081/actuator/health
```

### API Gateway

```text
GET http://localhost:8082/actuator/health
```

Example response:

```json
{
  "status": "UP"
}
```

These health endpoints are also used by Docker Compose health checks for the application containers.

They will later become part of the broader service-health monitoring and failure-detection system.

## Error Handling

The business services use centralized exception handling for common API failures.

The API Gateway additionally propagates downstream HTTP errors to clients.

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

The API Gateway also currently logs completed HTTP requests with:

```text
method
URI
status
correlationId
```

These logs establish the foundation for the structured logging and centralized incident-analysis pipeline that will be introduced in later observability stages.

Full JSON-based structured logging is intentionally deferred to the dedicated logging stage.

## Testing

The platform currently contains automated tests for the User Service, Order Service, and API Gateway.

### Order Service

The current Order Service test suite contains:

```text
15 tests
15 passed
0 failures
0 errors
```

Current coverage includes:

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

Run the Order Service tests with:

```bash
cd order-service
./mvnw clean test
```

### User Service

The current User Service test suite contains:

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

### API Gateway

The current Gateway test suite contains:

```text
1 test
1 passed
0 failures
0 errors
```

Run the Gateway tests with:

```bash
cd gateway
./mvnw clean test
```

The Gateway was also manually verified through end-to-end requests to both business services, including:

* User creation through Gateway
* User retrieval through Gateway
* User listing through Gateway
* User deletion through Gateway
* Order creation through Gateway
* Order retrieval through Gateway
* Order listing through Gateway
* Order update through Gateway
* Order deletion through Gateway
* Downstream `404` propagation
* Correlation ID preservation
* Correlation ID forwarding
* Basic request logging

### Dockerized End-to-End Verification

The complete Docker Compose platform was manually verified.

Successful verification included:

```text
Client
  ↓
API Gateway :8082
  ↓
User Service :8080
  ↓
PostgreSQL :5432
```

and:

```text
Client
  ↓
API Gateway :8082
  ↓
Order Service :8081
  ↓
PostgreSQL :5432
```

Both Gateway routes returned successful HTTP responses while running entirely inside the Docker Compose environment.

PostgreSQL persistence was also verified by restarting the PostgreSQL container and successfully retrieving previously stored user data afterward.

## Local Development

### Option 1 — Run the Complete Platform with Docker Compose

From the project root:

```bash
docker compose up --build
```

This is the recommended way to run the complete local platform.

Verify the containers:

```bash
docker compose ps
```

### Option 2 — Run PostgreSQL with Docker and Applications Locally

Start PostgreSQL:

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

### Run API Gateway

Open another terminal and navigate to:

```bash
cd gateway
```

Run:

```bash
./mvnw spring-boot:run
```

The Gateway starts on:

```text
http://localhost:8082
```

The recommended client flow is:

```text
Client
  ↓
API Gateway :8082
  ↓
User Service :8080
       OR
Order Service :8081
```

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

## API Gateway Examples

The Gateway provides the preferred client-facing API.

### Gateway Health Check

```bash
curl -i http://localhost:8082/actuator/health
```

### Create a User Through Gateway

```bash
curl -i -X POST http://localhost:8082/api/users \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: user-request-001" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com"
  }'
```

### Get a User Through Gateway

```bash
curl -i http://localhost:8082/api/users/1 \
  -H "X-Correlation-ID: user-request-002"
```

### Get All Users Through Gateway

```bash
curl -i http://localhost:8082/api/users \
  -H "X-Correlation-ID: user-request-003"
```

### Delete a User Through Gateway

```bash
curl -i -X DELETE http://localhost:8082/api/users/1 \
  -H "X-Correlation-ID: user-request-004"
```

### Create an Order Through Gateway

```bash
curl -i -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: order-request-001" \
  -d '{
    "userId": 1,
    "product": "Mechanical Keyboard",
    "quantity": 1,
    "amount": 129.99
  }'
```

### Get an Order Through Gateway

```bash
curl -i http://localhost:8082/api/orders/1 \
  -H "X-Correlation-ID: order-request-002"
```

### Get All Orders Through Gateway

```bash
curl -i http://localhost:8082/api/orders \
  -H "X-Correlation-ID: order-request-003"
```

### Update an Order Through Gateway

```bash
curl -i -X PUT http://localhost:8082/api/orders/1 \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: order-request-004" \
  -d '{
    "userId": 1,
    "product": "Mechanical Keyboard",
    "quantity": 2,
    "amount": 259.98,
    "status": "PROCESSING"
  }'
```

### Delete an Order Through Gateway

```bash
curl -i -X DELETE http://localhost:8082/api/orders/1 \
  -H "X-Correlation-ID: order-request-005"
```

### Verify Correlation ID

A request with an explicit correlation ID:

```bash
curl -i http://localhost:8082/api/orders/2 \
  -H "X-Correlation-ID: test-correlation-789"
```

returns the same correlation ID in the response:

```text
X-Correlation-ID: test-correlation-789
```

The Gateway also records the request:

```text
Request completed: method=GET uri=/api/orders/2 status=200 correlationId=test-correlation-789
```

## Docker Compose Verification

The complete platform can be verified with:

```bash
docker compose ps
```

Expected state:

```text
gateway          healthy
user-service     healthy
order-service    healthy
postgres         healthy
```

The application can then be tested through the Gateway:

```bash
curl -i http://localhost:8082/api/users
```

and:

```bash
curl -i http://localhost:8082/api/orders
```

Successful responses confirm:

```text
Client
  ↓
Docker Gateway
  ↓
Docker Service
  ↓
PostgreSQL
```

The PostgreSQL container can also be restarted:

```bash
docker compose restart postgres
```

Existing application data should remain available because PostgreSQL uses the persistent:

```text
postgres-data
```

Docker volume.

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

The complete project roadmap also includes testing, security hardening, CI/CD, and portfolio documentation.

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

**Stage 5 — Dockerization & Local Platform** ✅

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

### Stage 4 — API Gateway ✅

* Spring Boot API Gateway
* Centralized client-facing entry point
* User Service routing
* Order Service routing
* Request forwarding using Spring `RestClient`
* Correlation ID generation
* Correlation ID preservation
* Correlation ID propagation to downstream services
* Basic request-completion logging
* Downstream HTTP error propagation
* Gateway health endpoint
* Configurable User Service URL
* Configurable Order Service URL
* Gateway application-context test
* End-to-end routing verification
* Correlation ID verification
* Request logging verification
* 1/1 automated tests passing

### Stage 5 — Dockerization & Local Platform ✅

* Multi-stage Dockerfiles
* Containerized API Gateway
* Containerized User Service
* Containerized Order Service
* PostgreSQL container integration
* Docker bridge network
* Inter-service container communication
* Environment-based service configuration
* Container health checks
* Service dependency conditions
* Non-root application containers
* Persistent PostgreSQL Docker volume
* Complete Docker Compose application startup
* Gateway-to-User Service verification
* Gateway-to-Order Service verification
* PostgreSQL persistence verification
* Docker container health verification

### Current Architecture

The complete local application platform now consists of:

```text
                         Client
                           │
                           ▼
                  API Gateway :8082
                           │
                 ┌─────────┴─────────┐
                 │                   │
                 ▼                   ▼
          User Service         Order Service
             :8080                :8081
                 │                   │
                 └─────────┬─────────┘
                           ▼
                      PostgreSQL
                         :5432
                           │
                           ▼
                 Persistent Docker Volume
```

The entire application can now be started reproducibly through:

```bash
docker compose up --build
```

All application containers run as non-root users and participate in a dedicated Docker network.

Health checks and dependency conditions ensure that services start only after their required dependencies are available.

The database uses a persistent Docker volume so application data survives PostgreSQL container restarts.

### Next Stage

**Stage 6 — Application Observability** 🚧

The next stage will begin instrumenting the application for operational visibility.

Planned responsibilities include:

* Application metrics
* Micrometer integration
* HTTP request metrics
* Request latency measurement
* Error-rate measurement
* JVM metrics
* Service-level operational metrics
* Database-related metrics
* Metrics endpoints suitable for Prometheus scraping

This stage will establish the metrics foundation required for Prometheus, Grafana, Alertmanager, and the later AIOps incident-intelligence pipeline.

## License

This project is licensed under the [MIT License](LICENSE).

