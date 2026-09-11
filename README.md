# Monitoring & Observability Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Production-style microservice monitoring and observability platform with Spring Boot, PostgreSQL, Docker, Micrometer, Prometheus, Grafana, Alertmanager, structured logging, deterministic incident context generation, and a foundation for AI-powered incident intelligence.

## Overview

This project demonstrates a production-oriented monitoring and observability platform designed around a distributed microservice application.

The platform progressively introduces application services, database persistence, API gateway routing, containerization, health monitoring, application metrics, centralized Prometheus monitoring, Grafana dashboards, Alertmanager-based incident detection and routing, structured application logging, deterministic incident-context generation, and future AI-assisted AIOps capabilities.

The system is designed around a clear separation of responsibilities:

* **Applications** generate business traffic, metrics, logs, and health information.
* **API Gateway** provides a centralized client-facing entry point and propagates distributed request context.
* **Container Platform** provides reproducible local deployment, service networking, health checks, and persistent database storage.
* **Observability components** collect, store, visualize, and analyze operational data.
* **Prometheus** collects and stores application and infrastructure metrics and evaluates alerting rules.
* **Grafana** provides centralized operational dashboards for application, JVM, database, and service health monitoring.
* **Alertmanager** receives firing alerts from Prometheus, groups them, and routes them to the incident-context pipeline.
* **Incident Context Builder** collects and normalizes alert, metric, health, HTTP error, log, and timeline evidence into deterministic incident context.
* **AI components** are planned to analyze the generated incident context and provide recommendations.
* **Deterministic automation** remains responsible for executing operational changes.

The AI layer is therefore designed as a **read-only decision-support system**, rather than an autonomous system that directly modifies infrastructure or application state.

---

## Architecture

The current application and observability architecture is:

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


                  Application / Infrastructure Metrics
                                       │
                                       ▼
                              ┌──────────────────┐
                              │    Prometheus    │
                              │      :9090       │
                              └────────┬─────────┘
                                       │
                    ┌──────────────────┼──────────────────┐
                    │                  │                  │
                    ▼                  ▼                  ▼
             ┌─────────────┐   ┌──────────────┐   ┌──────────────┐
             │   Grafana   │   │ Alert Rules  │   │ Alertmanager │
             │    :3000    │   │              │   │    :9093     │
             └─────────────┘   └──────────────┘   └───────┬──────┘
                                                          │
                                                          ▼
                                               ┌──────────────────────┐
                                               │ Incident Context      │
                                               │ Builder               │
                                               │       :8090           │
                                               └──────────┬───────────┘
                                                          │
                                                          ▼
                                               ┌──────────────────────┐
                                               │ AI Incident Analyzer  │
                                               │       Planned         │
                                               └──────────────────────┘
```

The application services expose operational metrics through **Spring Boot Actuator and Micrometer**.

Prometheus centrally scrapes these metrics and evaluates application and infrastructure alert rules.

Grafana uses Prometheus as its data source and provides operational dashboards covering:

* Platform availability
* Application performance
* JVM health
* PostgreSQL health
* Service health

The current Grafana implementation contains **41 monitoring panels across 5 dashboards**.

Prometheus sends firing alerts to Alertmanager, which manages alert grouping, routing, and lifecycle state.

Alertmanager then forwards alerts to the **Incident Context Builder**, which collects and normalizes supporting operational evidence.

The complete application and observability stack can be started locally through Docker Compose.

---

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
* Micrometer
* Micrometer Prometheus Registry

### API Gateway

* Spring Boot
* Spring Web
* Spring `RestClient`
* Servlet Filters
* Correlation ID propagation
* Request-completion logging
* Centralized downstream error handling
* Spring Boot Actuator
* Micrometer

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
* Micrometer Prometheus Registry
* Prometheus
* Prometheus Alert Rules
* Node Exporter
* PostgreSQL Exporter
* Grafana
* Grafana provisioning
* Alertmanager
* Alert routing
* Alert grouping
* HTTP request metrics
* JVM metrics
* Process metrics
* System metrics
* Thread metrics
* Database connection pool metrics
* PostgreSQL database metrics
* Custom application metrics
* Structured JSON application logging
* Correlation IDs
* Incident context generation

### Incident Intelligence

* Alertmanager webhook ingestion
* Alert normalization
* Prometheus evidence collection
* Service health evidence collection
* HTTP error evidence collection
* Structured log parsing
* Deterministic incident timelines
* Best-effort evidence collection
* AI-powered incident analysis — planned

---

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
│
├── user-service/
│   ├── .mvn/
│   │   └── wrapper/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/ananyapraneet/monitoring/userservice/
│   │   │   │       ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── UserServiceApplication.java
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
│
├── order-service/
│   ├── .mvn/
│   │   └── wrapper/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/ananyapraneet/monitoring/orderservice/
│   │   │   │       ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── OrderServiceApplication.java
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
│
├── incident-context/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/ananyapraneet/monitoring/incidentcontext/
│   │   │   │       ├── config/
│   │   │   │       ├── controller/
│   │   │   │       ├── health/
│   │   │   │       ├── http/
│   │   │   │       ├── log/
│   │   │   │       ├── metrics/
│   │   │   │       ├── model/
│   │   │   │       ├── service/
│   │   │   │       ├── timeline/
│   │   │   │       ├── webhook/
│   │   │   │       └── IncidentContextApplication.java
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   ├── test/
│   │   │   └── java/
│   │   │       └── com/ananyapraneet/monitoring/incidentcontext/
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── mvnw
│
├── monitoring/
│   ├── prometheus/
│   │   ├── prometheus.yml
│   │   └── rules/
│   │       ├── application.yml
│   │       └── infrastructure.yml
│   │
│   ├── alertmanager/
│   │   └── alertmanager.yml
│   │
│   └── grafana/
│       ├── provisioning/
│       │   ├── datasources/
│       │   │   └── prometheus.yml
│       │   └── dashboards/
│       │       └── dashboards.yml
│       │
│       └── dashboards/
│           ├── platform-overview.json
│           ├── application-performance.json
│           ├── jvm.json
│           ├── database.json
│           └── service-health.json
│
├── ai-incident-analyzer/
│
├── docker-compose.yml
├── .env.example
├── README.md
└── .gitignore
```

The monitoring configuration is intentionally stored as code so that the Prometheus, Alertmanager, and Grafana environment can be recreated consistently.

Grafana dashboards and provisioning configuration are version-controlled JSON/YAML artifacts rather than dashboards that exist only inside the Grafana database.

---

# Implemented Services

## User Service

The User Service is the first independently deployable business service in the platform.

It currently provides:

```text
POST   /users
GET    /users
GET    /users/{id}
DELETE /users/{id}
```

### User Model

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
* Actuator metrics endpoint
* Micrometer instrumentation
* Prometheus metrics exposure
* Custom business metrics
* Docker containerization
* Container health check
* Structured JSON request logging
* Correlation ID support

The User Service runs on:

```text
http://localhost:8080
```

---

## Order Service

The Order Service is the second independently deployable business service in the platform.

It currently provides:

```text
POST   /orders
GET    /orders
GET    /orders/{id}
PUT    /orders/{id}
DELETE /orders/{id}
```

### Order Model

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

### Order Status

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
* Structured JSON request logging
* Correlation ID support
* Actuator health endpoint
* Actuator metrics endpoint
* Micrometer instrumentation
* Prometheus metrics exposure
* Custom business metrics
* Docker containerization
* Container health check

The Order Service runs on:

```text
http://localhost:8081
```

---

# API Gateway

The API Gateway provides a centralized entry point for clients accessing the business services.

The Gateway runs on:

```text
http://localhost:8082
```

## User Routes

```text
POST   /api/users
GET    /api/users
GET    /api/users/{id}
DELETE /api/users/{id}
```

These routes are forwarded to the User Service:

```text
/api/users/* → http://user-service:8080/users/*
```

## Order Routes

```text
POST   /api/orders
GET    /api/orders
GET    /api/orders/{id}
PUT    /api/orders/{id}
DELETE /api/orders/{id}
```

These routes are forwarded to the Order Service:

```text
/api/orders/* → http://order-service:8081/orders/*
```

The Gateway currently provides:

* Centralized client-facing entry point
* Request routing
* Request forwarding
* Correlation ID generation
* Correlation ID preservation
* Correlation ID propagation
* Request-completion logging
* Downstream HTTP error propagation
* Gateway health endpoint
* Gateway metrics endpoint
* Micrometer HTTP instrumentation
* Prometheus metrics exposure
* Configurable downstream service URLs
* Docker containerization
* Container health check

---

# Correlation IDs

The platform uses the:

```text
X-Correlation-ID
```

HTTP header to associate requests across the distributed application.

If a client provides a correlation ID, the Gateway preserves it.

If no correlation ID is provided, the Gateway generates a UUID.

The correlation ID is forwarded to downstream services.

The application services also place the correlation ID into the logging context so that structured logs can be associated with individual requests.

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
  │
  │ requestId = abc-123
  ▼
Structured Application Log
```

This establishes the foundation for distributed request observability and incident evidence correlation.

---

# Structured Application Logging

The platform implements structured JSON request logging in the Gateway, User Service, and Order Service.

Each completed HTTP request can produce machine-readable logging information including:

```text
timestamp
level
requestId
service
logger
message
```

The request-completion log contains information such as:

```text
HTTP method
endpoint
HTTP status
request duration
```

Example:

```json
{
  "timestamp": "2026-09-11T09:18:33.429500445Z",
  "level": "INFO",
  "requestId": "5a0dcbed-3b55-46ce-8ee8-75729cae4799",
  "service": "order-service",
  "message": "HTTP request completed: method=GET endpoint=/orders/999999999 status=404 durationMs=911"
}
```

The logging implementation uses correlation IDs through the application logging context.

This makes application logs suitable for machine processing and future centralized log ingestion.

---

# Gateway Error Handling

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

---

# Service Independence

The platform currently contains two independently deployable Spring Boot business services, one API Gateway, and a dedicated incident-context service:

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


                         Alertmanager
                              │
                              ▼
                    Incident Context Builder
                            :8090
```

Both business services use the same PostgreSQL instance during local development while maintaining **separate database schemas and Flyway migration histories**.

This provides service-level schema isolation while keeping the local development environment lightweight.

---

# Dockerized Local Platform

The complete application and observability stack can be started through Docker Compose.

The Dockerized platform consists of:

```text
postgres
user-service
order-service
gateway
incident-context
prometheus
alertmanager
node-exporter
postgres-exporter
grafana
```

All services communicate through the dedicated Docker bridge network:

```text
monitoring-network
```

## Internal Service Communication

```text
Gateway → http://user-service:8080
Gateway → http://order-service:8081

User Service → postgres:5432
Order Service → postgres:5432

Prometheus → gateway:8082/actuator/prometheus
Prometheus → user-service:8080/actuator/prometheus
Prometheus → order-service:8081/actuator/prometheus
Prometheus → node-exporter:9100
Prometheus → postgres-exporter:9187

Grafana → http://prometheus:9090

Prometheus → alertmanager:9093

Alertmanager → http://incident-context:8090/api/v1/alerts

Incident Context Builder → http://prometheus:9090
Incident Context Builder → http://user-service:8080
Incident Context Builder → http://order-service:8081
```

## Start the Complete Platform

From the project root:

```bash
docker compose up --build
```

The command builds the application images and starts the application and observability components.

## Verify Containers

Run:

```bash
docker compose ps
```

Expected services include:

```text
monitoring-postgres
monitoring-user-service
monitoring-order-service
monitoring-api-gateway
monitoring-incident-context
monitoring-prometheus
monitoring-alertmanager
monitoring-node-exporter
monitoring-postgres-exporter
monitoring-grafana
```

The application services and Incident Context Builder use Docker health checks.

---

## Container Health Checks

The Compose configuration includes health checks for the core application services.

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

Incident Context Builder exposes its own health endpoint and Alertmanager waits for the Incident Context Builder to become healthy before activating its webhook dependency.

This establishes the following dependency chain:

```text
PostgreSQL
    │
    ├──────────────► User Service
    │
    └──────────────► Order Service
                           │
                           ▼
                     API Gateway


Prometheus
    │
    ▼
Alertmanager
    │
    ▼
Incident Context Builder
```

Prometheus depends on the application services being available before starting its monitoring workload.

---

## Non-Root Containers

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

---

## Persistent PostgreSQL Storage

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

---

## Prometheus Storage

Prometheus uses a named Docker volume:

```text
prometheus-data
```

The volume is mounted to:

```text
/prometheus
```

This provides persistent Prometheus time-series storage across container recreation.

---

## Grafana Storage

Grafana uses a named Docker volume:

```text
grafana-data
```

The volume is mounted to:

```text
/var/lib/grafana
```

Dashboard definitions themselves are maintained as version-controlled JSON files under:

```text
monitoring/grafana/dashboards/
```

Grafana provisioning configuration is maintained under:

```text
monitoring/grafana/provisioning/
```

This keeps the dashboard environment reproducible through source control.

---

## Alertmanager Storage

Alertmanager uses a named Docker volume:

```text
alertmanager-data
```

The volume is mounted to:

```text
/alertmanager
```

This provides persistent Alertmanager state across container recreation.

---

## Docker Network

All services are attached to:

```text
monitoring-network
```

The network allows containers to communicate using Compose service names rather than host-specific addresses.

This produces a reproducible local environment that closely resembles a multi-service deployment topology.

---

# Database

The platform currently uses PostgreSQL as its primary relational database.

PostgreSQL is started through Docker Compose:

```bash
docker compose up -d postgres
```

Or as part of the complete application and observability platform:

```bash
docker compose up --build
```

The PostgreSQL container is exposed locally on:

```text
localhost:5432
```

## User Service Schema

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

## Order Service Schema

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

---

# Health Checks

Spring Boot Actuator provides health endpoints for all application services, the Gateway, and the Incident Context Builder.

## User Service

```text
GET http://localhost:8080/actuator/health
```

## Order Service

```text
GET http://localhost:8081/actuator/health
```

## API Gateway

```text
GET http://localhost:8082/actuator/health
```

## Incident Context Builder

```text
GET http://localhost:8090/actuator/health
```

Example response:

```json
{
  "groups": [
    "liveness",
    "readiness"
  ],
  "status": "UP"
}
```

These health endpoints are also used by Docker Compose health checks for the application containers.

They form the foundation for service-health monitoring and failure detection.

---

# Application Observability

## Actuator Endpoints

The application services and Gateway expose the following Actuator endpoints:

```text
/actuator/health
/actuator/info
/actuator/metrics
/actuator/prometheus
```

The Incident Context Builder exposes health and information endpoints required for its operational lifecycle.

The Prometheus endpoint exposes Micrometer metrics in Prometheus exposition format.

Examples:

```text
http://localhost:8080/actuator/prometheus
http://localhost:8081/actuator/prometheus
http://localhost:8082/actuator/prometheus
```

The `/actuator/metrics` endpoint provides access to individual operational measurements collected by Micrometer.

Examples:

```text
http://localhost:8080/actuator/metrics
http://localhost:8081/actuator/metrics
http://localhost:8082/actuator/metrics
```

---

# Micrometer

Micrometer provides the instrumentation layer used by the Spring Boot applications.

The platform collects standard operational metrics covering:

* HTTP requests
* Request duration
* HTTP status codes
* HTTP methods
* Request outcomes
* Exceptions
* JVM memory
* JVM threads
* Garbage collection
* Process CPU usage
* System CPU usage
* Disk usage
* Executor activity
* Tomcat activity
* Database connection pools
* JDBC connection activity

Spring Boot's built-in:

```text
http.server.requests
```

metric provides request-level measurements including:

```text
method
URI
status
outcome
error
exception
COUNT
TOTAL_TIME
MAX
```

Histogram buckets are enabled for HTTP request duration so that Prometheus can calculate percentile latency values such as:

```text
P50
P95
P99
```

---

# Custom Application Metrics

In addition to the standard Micrometer metrics, the business services expose custom metrics representing important application-level events.

## User Service Metrics

### User Creation Counter

```text
user_creation_total
```

Description:

```text
Total number of users successfully created
```

### Service Request Counter

```text
service_requests_total
```

Description:

```text
Total number of requests handled by the User Service
```

## Order Service Metrics

### Successful Order Creation Counter

```text
orders_created_total
```

Description:

```text
Total number of orders successfully created
```

### Failed Order Creation Counter

```text
orders_failed_total
```

Description:

```text
Total number of failed order creation attempts
```

The implementation uses `saveAndFlush()` so persistence failures occur inside the service's error-handling boundary and can be recorded by the custom failure counter.

---

# HTTP Request Metrics

The standard Micrometer metric:

```text
http.server.requests
```

is exposed by the User Service, Order Service, and API Gateway.

It records information including:

```text
HTTP method
Request URI
HTTP status
Request outcome
Exception
Error
Request count
Total request time
```

Prometheus exposes the metric as:

```text
http_server_requests_seconds_count
http_server_requests_seconds_sum
http_server_requests_seconds_bucket
http_server_requests_seconds_max
```

These metrics are used by the Grafana dashboards to calculate:

* Request rate
* HTTP status distribution
* Error rate
* P50 latency
* P95 latency
* P99 latency
* Requests by HTTP method
* Top request URIs

---

# JVM and Runtime Metrics

The applications expose JVM and process-level metrics through Micrometer.

Examples include:

```text
jvm_memory_used_bytes
jvm_memory_committed_bytes
jvm_memory_max_bytes

jvm_threads_live_threads
jvm_threads_daemon_threads
jvm_threads_peak_threads
jvm_threads_started_threads

process_cpu_usage
process_cpu_time_seconds_total

system_cpu_usage
system_cpu_count
```

Additional metrics cover:

* Garbage collection
* JVM buffers
* Class loading
* Compilation
* Executor activity
* Tomcat activity
* Disk space
* Process uptime

These metrics provide visibility into:

* JVM memory utilization
* JVM thread activity
* CPU consumption
* Garbage collection
* Runtime resource pressure
* Application process health

---

# Database Metrics

The Spring Boot applications expose database connection pool and JDBC metrics.

Examples include:

```text
hikaricp.connections
hikaricp.connections.active
hikaricp.connections.idle
hikaricp.connections.max
hikaricp.connections.min

jdbc.connections.active
jdbc.connections.idle
jdbc.connections.max
jdbc.connections.min
```

These metrics provide visibility into application-side database connection utilization.

In addition, the platform uses **PostgreSQL Exporter** to expose PostgreSQL server-level database metrics.

The PostgreSQL exporter runs on:

```text
localhost:9187
```

and exposes:

```text
/metrics
```

Prometheus scrapes the exporter through:

```text
postgres-exporter:9187
```

Important PostgreSQL metrics include:

```text
pg_up
pg_database_size_bytes
pg_database_connection_limit
pg_settings_max_connections
pg_stat_database_numbackends
pg_stat_database_xact_commit
pg_stat_database_xact_rollback
pg_stat_database_blks_hit
pg_stat_database_blks_read
pg_stat_database_tup_fetched
pg_stat_database_tup_inserted
pg_stat_database_tup_updated
pg_stat_database_tup_deleted
pg_stat_database_deadlocks
pg_stat_database_temp_files
pg_stat_database_temp_bytes
pg_stat_bgwriter_buffers_alloc_total
pg_stat_bgwriter_buffers_clean_total
```

These metrics provide visibility into:

* PostgreSQL availability
* Active connections
* Database size
* Transaction activity
* Tuple activity
* Cache hit ratio
* Deadlocks
* Temporary file activity
* Background writer activity

---

# Prometheus Monitoring

## Prometheus

Prometheus provides centralized metrics collection and time-series storage for the platform.

The Prometheus server runs on:

```text
http://localhost:9090
```

Prometheus is configured with a:

```text
15 second scrape interval
```

and:

```text
15 second evaluation interval
```

## Prometheus Scrape Targets

The current Prometheus configuration scrapes:

```text
gateway
user-service
order-service
node-exporter
postgres-exporter
```

Application targets expose:

```text
/actuator/prometheus
```

The infrastructure and database exporters expose their standard:

```text
/metrics
```

endpoint.

The current scrape topology is:

```text
Gateway ──────────────┐
User Service ─────────┤
Order Service ────────┼──► Prometheus
                      │
Node Exporter ────────┤
                      │
PostgreSQL Exporter ──┘
```

---

# Prometheus Alert Rules

Prometheus currently evaluates application, infrastructure, and database alert rules.

The rules are maintained as source-controlled files:

```text
monitoring/prometheus/rules/application.yml
monitoring/prometheus/rules/infrastructure.yml
```

The current implementation contains:

```text
4 application alerts
5 infrastructure/database alerts
────────────────────────────
9 total alert rules
```

Required alert metadata includes:

```text
service
severity
environment
alertname
instance
```

## Application Alerts

### ServiceDown

Detects an unavailable application Prometheus target.

```text
up{job=~"gateway|user-service|order-service"} == 0
```

Condition:

```text
1 minute
```

Severity:

```text
critical
```

### HighHttp5xxErrorRate

Detects HTTP 5xx responses exceeding:

```text
5%
```

for:

```text
5 minutes
```

Severity:

```text
warning
```

### HighRequestLatency

Detects P95 HTTP request latency above:

```text
1 second
```

for:

```text
5 minutes
```

Severity:

```text
warning
```

### HighJvmMemoryUsage

Detects JVM heap utilization above:

```text
85%
```

for:

```text
5 minutes
```

Severity:

```text
warning
```

## Infrastructure Alerts

### HighCpuUsage

Detects infrastructure CPU utilization above:

```text
80%
```

for:

```text
5 minutes
```

Severity:

```text
warning
```

### HighMemoryUsage

Detects infrastructure memory utilization above:

```text
85%
```

for:

```text
5 minutes
```

Severity:

```text
warning
```

### LowFilesystemSpace

Detects filesystem availability below:

```text
15%
```

for:

```text
5 minutes
```

Severity:

```text
warning
```

### HighSystemLoad

Detects one-minute system load above:

```text
4
```

for:

```text
5 minutes
```

Severity:

```text
warning
```

> **Note:** Node Exporter is currently running inside the Docker environment on macOS. Its infrastructure metrics therefore represent the Linux environment available to the containerized stack rather than the physical Mac host's 
hardware resources.

### DatabaseConnectionExhaustion

Detects PostgreSQL connection utilization above:

```text
90%
```

of the configured maximum connections.

Condition:

```text
5 minutes
```

Severity:

```text
warning
```

The alert uses PostgreSQL Exporter metrics:

```text
pg_stat_database_numbackends
pg_settings_max_connections
```

---

# Alert Metadata

Each alert is designed to carry enough information for an operational responder to understand the incident context.

## Labels

Alerts include:

```text
alertname
service
severity
environment
instance
```

Example:

```text
alertname: ServiceDown
service: gateway
severity: critical
environment: local
instance: gateway:8082
```

## Annotations

Each alert includes:

```text
summary
description
runbook
```

Example:

```text
summary:
gateway is unavailable

description:
gateway has been unavailable for more than 1 minute.

runbook:
Check the service container, application logs,
health endpoint, and downstream dependencies.
```

The metadata is consumed by the Incident Context Builder during alert normalization.

---

# Alertmanager

Alertmanager provides the alert-management layer between Prometheus and the Incident Context Builder.

Alertmanager runs on:

```text
http://localhost:9093
```

The configuration is maintained at:

```text
monitoring/alertmanager/alertmanager.yml
```

## Prometheus → Alertmanager

Prometheus is configured to send alerts to:

```text
http://alertmanager:9093
```

The resulting flow is:

```text
Prometheus
    │
    │ firing alerts
    ▼
Alertmanager
    │
    ├── grouping
    ├── routing
    └── lifecycle management
```

## Alert Grouping

Alertmanager groups alerts by:

```text
alertname
service
severity
environment
```

The current grouping configuration uses:

```text
group_wait: 30s
group_interval: 5m
repeat_interval: 4h
```

## Incident Context Receiver

The active Alertmanager receiver is:

```text
incident-context
```

It forwards alerts to:

```text
http://incident-context:8090/api/v1/alerts
```

The resulting pipeline is:

```text
Prometheus
    │
    ▼
Alertmanager
    │
    │ webhook
    ▼
Incident Context Builder
```

This replaces the earlier local-only default receiver and establishes the incident-data pipeline.

## Alertmanager Configuration Validation

The Alertmanager configuration was validated using:

```bash
docker run --rm \
  --entrypoint amtool \
  -v "$(pwd)/monitoring/alertmanager/alertmanager.yml:/etc/alertmanager/alertmanager.yml:ro" \
  prom/alertmanager:v0.29.0 \
  check-config /etc/alertmanager/alertmanager.yml
```

The configuration passed validation successfully.

---

# Incident Data Pipeline

The Incident Data Pipeline is the foundation for the platform's future AI incident-intelligence layer.

Its responsibility is to collect operational evidence **before** that evidence is passed to an AI analyzer.

The current architecture is:

```text
                         Alertmanager
                              │
                              │ webhook
                              ▼
                    ┌─────────────────────┐
                    │ Incident Context    │
                    │ Builder :8090       │
                    └──────────┬──────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
          Prometheus       Service Health    HTTP Errors
          Metrics             │                │
              │               │                │
              └───────────────┼────────────────┘
                              │
                              ▼
                     Structured Log Parser
                              │
                              ▼
                       Timeline Builder
                              │
                              ▼
                     Incident Context JSON
                              │
                              ▼
                    AI Incident Analyzer
                         (planned)
```

The Incident Context Builder is intentionally a separate service from the AI analyzer.

This creates a controlled boundary between:

```text
Evidence Collection
```

and:

```text
AI Interpretation
```

The AI layer therefore does not need unrestricted direct access to Prometheus, application services, or raw infrastructure data.

---

# Incident Context Builder

The Incident Context Builder runs on:

```text
http://localhost:8090
```

Health endpoint:

```text
GET /actuator/health
```

Alert ingestion endpoint:

```text
POST /api/v1/alerts
```

## Alertmanager Webhook

The service accepts Alertmanager webhook payloads containing:

```text
receiver
status
groupKey
truncatedAlerts
alerts
```

Each alert can contain:

```text
status
labels
annotations
startsAt
endsAt
generatorURL
fingerprint
```

The incoming alert is normalized into a stable internal representation.

---

# Alert Evidence

Alertmanager alerts are normalized into:

```text
AlertEvidence
```

The normalized representation includes:

```text
alertName
status
severity
service
instance
summary
description
runbook
labels
```

This removes dependency on the exact structure of the upstream Alertmanager payload while preserving the operational information required for incident analysis.

---

# Prometheus Metric Evidence

The Incident Context Builder queries Prometheus for service-level evidence.

Current metrics include:

```text
http_5xx_rate
request_rate
```

The service uses PromQL based on the affected application's Prometheus job:

```text
http_server_requests_seconds_count{job="<service>"}
```

For HTTP 5xx evidence:

```text
sum(
  rate(
    http_server_requests_seconds_count{
      job="<service>",
      status=~"5.."
    }[5m]
  )
)
```

For request rate:

```text
sum(
  rate(
    http_server_requests_seconds_count{
      job="<service>"
    }[5m]
  )
)
```

The collected Prometheus response is preserved as structured evidence rather than converted into fabricated values.

---

# Health Evidence

The Incident Context Builder can query the health endpoint of the affected application service.

Currently supported application services are:

```text
user-service
order-service
```

Configured internal URLs are:

```text
http://user-service:8080
http://order-service:8081
```

Health evidence is represented as:

```text
HealthEvidence
```

containing:

```text
status
components
```

Example:

```json
{
  "status": "UP",
  "components": {}
}
```

Unknown services are handled safely and produce:

```json
{
  "status": "UNKNOWN",
  "components": {}
}
```

---

# HTTP Error Evidence

The Incident Context Builder queries Prometheus for HTTP 4xx and 5xx request metrics associated with the affected service.

The evidence is normalized into:

```text
HttpErrorEvidence
```

containing:

```text
timestamp
method
endpoint
status
service
requestId
message
```

Example:

```json
{
  "timestamp": "2026-09-11T11:02:32.926Z",
  "method": "GET",
  "endpoint": "/orders/{id}",
  "status": 404,
  "service": "order-service",
  "requestId": null,
  "message": null
}
```

This allows an incident context to identify concrete HTTP failures associated with an affected service.

---

# Structured Log Evidence

The application services produce structured JSON logs containing fields such as:

```text
timestamp
level
requestId
service
message
exception
```

The Incident Context Builder includes a structured-log parser capable of converting these JSON records into:

```text
LogEvidence
```

The parser validates required fields and safely ignores malformed records.

The current production log client intentionally returns an empty collection because a centralized log-ingestion backend has **not yet been implemented**.

This is deliberate.

The Incident Context Builder does **not** read Docker container logs directly and does not depend on the Docker socket.

The current architecture therefore separates:

```text
Structured Log Format
        │
        ▼
Structured Log Parser
        │
        ▼
Future Centralized Log Backend
```

Centralized log ingestion will be introduced in a later observability stage.

---

# Deterministic Incident Timeline

The Incident Context Builder creates a deterministic timeline from available evidence.

Timeline events currently include:

```text
ALERT_FIRING
ALERT_RESOLVED
HTTP_ERROR
LOG_EVENT
```

Events are ordered chronologically.

Alert start events are created from:

```text
startsAt
```

Resolved alert events are created only when a valid Alertmanager end timestamp is available.

The Alertmanager placeholder timestamp:

```text
0001-01-01T00:00:00Z
```

is ignored.

Null or invalid timestamps are ignored rather than fabricated.

HTTP error timestamps come from Prometheus samples.

Structured log timestamps come from the structured log records.

The timeline therefore represents observed evidence rather than synthetic incident events.

---

# Incident Context Format

The Incident Context Builder produces a deterministic JSON structure:

```json
{
  "incident": "APIErrorRateHigh",
  "severity": "HIGH",
  "service": "order-service",
  "alerts": [],
  "metrics": {},
  "logs": [],
  "health": {},
  "httpErrors": [],
  "timeline": []
}
```

The complete domain model contains:

```text
IncidentContext
AlertEvidence
LogEvidence
HealthEvidence
HttpErrorEvidence
TimelineEvent
```

The structure provides a stable contract for future AI incident analysis.

---

# Incident Context Example

A controlled Docker E2E request using:

```text
APIErrorRateHigh
```

for:

```text
order-service
```

produced incident context containing:

```text
Incident:
APIErrorRateHigh

Severity:
HIGH

Service:
order-service

Metrics:
http_5xx_rate
request_rate

Health:
UP

HTTP Errors:
GET /orders/{id} → 404

Logs:
empty
```

The generated timeline contained:

```text
10:00:00Z  ALERT_FIRING
11:02:32Z  HTTP_ERROR
```

This demonstrates that the Incident Context Builder can combine independent evidence sources into a single deterministic incident representation.

---

# Incident Pipeline Resilience

The incident pipeline is designed around **best-effort evidence collection**.

A failure in one evidence source should not cause the entire incident context request to fail.

## Empty Alert List

If Alertmanager sends an empty alert list, the service returns:

```json
{
  "incident": "UnknownIncident",
  "severity": "unknown",
  "service": "unknown",
  "alerts": [],
  "metrics": {},
  "logs": [],
  "health": {
    "status": "UNKNOWN",
    "components": {}
  },
  "httpErrors": [],
  "timeline": []
}
```

No server error is generated.

## Missing Labels or Annotations

Missing Alertmanager labels and annotations are normalized safely.

The service does not fail when optional alert metadata is absent.

## Unknown Service

If an alert references an unknown service:

```text
payment-service
```

the Incident Context Builder safely handles the unknown service.

The unsupported health check returns:

```text
UNKNOWN
```

and the evidence pipeline continues without generating an application error.

## Prometheus Unavailable

If Prometheus is unavailable, the Prometheus clients return empty evidence rather than failing the complete incident-context request.

Metrics therefore degrade to empty collections while the rest of the incident context can still be constructed.

This behavior prevents a temporary monitoring-backend failure from turning an incident webhook into a `500 Internal Server Error`.

---

# Incident Data Pipeline Verification

The complete Incident Data Pipeline was verified inside the Docker Compose environment.

The verified path is:

```text
Alertmanager
     │
     ▼
incident-context:8090
     │
     ├──► Prometheus :9090
     │
     ├──► order-service :8081
     │
     ├──► HTTP error evidence
     │
     └──► deterministic timeline
```

A controlled Alertmanager-compatible webhook request was submitted to:

```text
POST http://localhost:8090/api/v1/alerts
```

The resulting response successfully contained:

```text
APIErrorRateHigh
HIGH
order-service
Prometheus metrics
UP health status
HTTP 404 evidence
Timeline events
```

The actual Docker E2E response included:

```text
incident = APIErrorRateHigh
severity = HIGH
service = order-service
health = UP
```

and HTTP error evidence:

```text
GET /orders/{id}
status = 404
```

The timeline contained:

```text
ALERT_FIRING
HTTP_ERROR
```

This verified:

* Alert webhook ingestion
* Alert normalization
* Prometheus connectivity
* Metric collection
* Service health collection
* HTTP error collection
* Timeline construction
* Docker networking
* Incident Context Builder container health

---

# Testing

The Incident Context Builder currently contains automated tests covering its major components.

## Incident Context Builder Test Suite

Current final test result:

```text
22 tests
22 passed
0 failures
0 errors
```

The test suite covers:

* Prometheus metric queries
* Prometheus unavailable behavior
* Incident context model serialization
* Health HTTP client behavior
* Alertmanager webhook controller
* HTTP error evidence collection
* Prometheus unavailable HTTP-error behavior
* Structured log parsing
* Incident context building
* Timeline generation
* Alert normalization
* Application context startup

The full test suite is run with:

```bash
./mvnw test
```

from:

```text
incident-context/
```

The final Stage 11 implementation passed:

```text
Tests run: 22
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

---

## Order Service

The current Order Service test suite contains:

```text
16 tests
16 passed
0 failures
0 errors
```

Run:

```bash
cd order-service
./mvnw clean test
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
* Successful order creation metric
* Failed order creation metric

---

## User Service

The current User Service test suite contains:

```text
8 tests
8 passed
0 failures
0 errors
```

Run:

```bash
cd user-service
./mvnw clean test
```

---

## API Gateway

The current Gateway test suite contains:

```text
1 test
1 passed
0 failures
0 errors
```

Run:

```bash
cd gateway
./mvnw clean test
```

The Gateway was also manually verified through end-to-end requests to both business services.

---

# Grafana Dashboards

Grafana provides the operational visualization layer for the Prometheus metrics.

Grafana runs on:

```text
http://localhost:3000
```

Prometheus is configured as the default Grafana data source:

```text
http://prometheus:9090
```

The data source and dashboards are provisioned automatically through source-controlled configuration.

The current implementation contains **5 dashboards with 41 panels**.

Available dashboards:

```text
Platform Overview
Application Performance
JVM
Database
Service Health
```

---

## Dashboard 1 — Platform Overview

### Panels

```text
1. Gateway Availability
2. User Service Availability
3. Order Service Availability
4. PostgreSQL Availability
5. Request Rate
6. HTTP 5xx Error Rate
7. P50 Request Latency
8. P95 Request Latency
9. P99 Request Latency
```

---

## Dashboard 2 — Application Performance

### Panels

```text
1. Requests / Second
2. P50 Request Latency
3. P95 Request Latency
4. P99 Request Latency
5. HTTP Status Distribution
6. Requests by HTTP Method
7. Top Request URIs
```

---

## Dashboard 3 — JVM

### Panels

```text
1. JVM Heap Usage %
2. JVM Heap Used
3. JVM Heap Max
4. JVM Non-Heap Used
5. JVM Garbage Collection Rate
6. JVM Live Threads
7. JVM CPU Usage
```

---

## Dashboard 4 — Database

### Panels

```text
1. PostgreSQL Availability
2. Active Connections
3. Database Size
4. Transactions / Second
5. Tuples Fetched / Second
6. Tuple Modifications / Second
7. Database Cache Hit Ratio
8. Deadlocks
9. Temporary Files / Second
10. Background Writer Activity
```

---

## Dashboard 5 — Service Health

### Panels

```text
1. Gateway Health
2. User Service Health
3. Order Service Health
4. PostgreSQL Health
5. All Services Availability
6. Service Request Rate
7. Service Error Rate
8. Service P95 Latency
```

---

# Grafana Provisioning

Grafana is configured through source-controlled provisioning files.

## Prometheus Data Source

The Prometheus data source is automatically provisioned using:

```text
monitoring/grafana/provisioning/datasources/prometheus.yml
```

The configured endpoint is:

```text
http://prometheus:9090
```

## Dashboard Provider

Dashboard provisioning is configured using:

```text
monitoring/grafana/provisioning/dashboards/dashboards.yml
```

The provider loads dashboard definitions from:

```text
/var/lib/grafana/dashboards
```

The Docker Compose configuration mounts:

```text
./monitoring/grafana/dashboards
```

into the Grafana container.

This allows dashboards to be recreated automatically from Git-controlled JSON definitions.

---

# Observability Verification

The monitoring pipeline has been verified end-to-end.

## Application Metrics

All three Spring Boot services successfully expose:

```text
/actuator/prometheus
```

Prometheus successfully scrapes:

```text
gateway
user-service
order-service
```

## Infrastructure Metrics

Node Exporter successfully exposes infrastructure metrics through:

```text
node-exporter:9100
```

Prometheus successfully scrapes the exporter.

## PostgreSQL Metrics

PostgreSQL Exporter successfully exposes database metrics through:

```text
postgres-exporter:9187
```

The following Prometheus query was verified:

```promql
pg_up
```

and returned:

```text
1
```

## HTTP Metrics

The application services expose:

```text
http_server_requests_seconds_count
http_server_requests_seconds_sum
http_server_requests_seconds_bucket
http_server_requests_seconds_max
```

Prometheus queries were successfully validated for:

```text
Request rate
P50 latency
P95 latency
P99 latency
HTTP 5xx rate
HTTP status distribution
HTTP method distribution
Top request URIs
```

## JVM Metrics

Prometheus successfully exposes and queries JVM metrics including:

```text
jvm_memory_used_bytes
jvm_memory_max_bytes
jvm_memory_committed_bytes
jvm_threads_live_threads
jvm_gc_pause_seconds_count
process_cpu_usage
```

## Alert Rules

Prometheus successfully loaded and evaluated:

```text
4 application alert rules
5 infrastructure/database alert rules
────────────────────────────────
9 total alert rules
```

The Prometheus configuration and rule files were validated using `promtool`.

## Alertmanager

Alertmanager successfully:

* Started with the configured configuration
* Loaded the routing configuration
* Connected to Prometheus
* Received a real `ServiceDown` alert
* Reported the alert as active
* Cleared the alert after service recovery
* Forwarded alerts to the Incident Context Builder

## Incident Context Builder

The Incident Context Builder successfully:

* Started as a Docker Compose service
* Passed its Docker health check
* Accepted Alertmanager webhook payloads
* Normalized Alertmanager alerts
* Queried Prometheus
* Queried application health endpoints
* Collected HTTP error evidence
* Parsed structured log records through its parser
* Constructed deterministic timelines
* Returned resilient responses when evidence sources were unavailable

## Grafana

All five dashboards were successfully provisioned into Grafana.

The dashboards were visually validated and the implemented panels returned live data.

Current dashboard inventory:

```text
Platform Overview        9 panels
Application Performance  7 panels
JVM                      7 panels
Database                10 panels
Service Health            8 panels
───────────────────────────────
Total                    41 panels
```

---

# Application Metrics Verification

## User Service Metrics

The User Service successfully exposes:

```text
/actuator/metrics
/actuator/prometheus
```

Standard metrics such as:

```text
http.server.requests
hikaricp.connections
jvm.memory.used
jvm.threads.live
process.cpu.usage
system.cpu.usage
```

were verified at runtime.

The custom metric:

```text
user_creation_total
```

was verified by creating a user and observing the counter increment.

The custom metric:

```text
service_requests_total
```

was verified by generating a User Service request and observing the counter increment.

## Order Service Metrics

The Order Service successfully exposes:

```text
/actuator/metrics
/actuator/prometheus
```

The standard:

```text
http.server.requests
```

metric was verified with HTTP status and outcome dimensions.

The custom metrics:

```text
orders_created_total
orders_failed_total
```

were registered and verified.

## API Gateway Metrics

The Gateway successfully exposes:

```text
/actuator/metrics
/actuator/prometheus
```

The available metrics include:

```text
http.server.requests
jvm.memory.used
jvm.threads.live
process.cpu.usage
system.cpu.usage
disk.free
disk.total
```

Gateway HTTP metrics were verified using:

```bash
curl -s http://localhost:8082/actuator/metrics/http.server.requests
```

The metric captures:

```text
HTTP method
URI
status
outcome
exception
error
request count
total request time
maximum request time
```

---

# Error Handling

The business services use centralized exception handling for common API failures.

The API Gateway additionally propagates downstream HTTP errors to clients.

## Validation Failure

```text
HTTP 400 Bad Request
```

Example validation scenarios include:

* Missing required fields
* Blank product or name values
* Invalid quantity
* Invalid amount
* Invalid email

## Resource Not Found

```text
HTTP 404 Not Found
```

## Duplicate User

The User Service returns:

```text
HTTP 409 Conflict
```

when attempting to create a user with an existing email address.

## Successful Deletion

Successful deletion returns:

```text
HTTP 204 No Content
```

---

# API Examples

## User Service

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Metrics

```bash
curl http://localhost:8080/actuator/metrics
```

### Prometheus Metrics

```bash
curl http://localhost:8080/actuator/prometheus
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

---

## Order Service

### Health Check

```bash
curl http://localhost:8081/actuator/health
```

### Metrics

```bash
curl http://localhost:8081/actuator/metrics
```

### Prometheus Metrics

```bash
curl http://localhost:8081/actuator/prometheus
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

---

## API Gateway

### Gateway Health Check

```bash
curl -i http://localhost:8082/actuator/health
```

### Gateway Metrics

```bash
curl -i http://localhost:8082/actuator/metrics
```

### Gateway Prometheus Metrics

```bash
curl -i http://localhost:8082/actuator/prometheus
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

### Verify Correlation ID

```bash
curl -i http://localhost:8082/api/orders/2 \
  -H "X-Correlation-ID: test-correlation-789"
```

The response should preserve:

```text
X-Correlation-ID: test-correlation-789
```

---

## Incident Context Builder

### Health Check

```bash
curl -s http://localhost:8090/actuator/health
```

### Submit Alertmanager-Compatible Incident

```bash
curl -sS -X POST http://localhost:8090/api/v1/alerts \
  -H 'Content-Type: application/json' \
  -d '{
    "receiver": "incident-context",
    "status": "firing",
    "groupKey": "docker-e2e-test",
    "truncatedAlerts": 0,
    "alerts": [
      {
        "status": "firing",
        "labels": {
          "alertname": "APIErrorRateHigh",
          "severity": "HIGH",
          "service": "order-service",
          "environment": "test"
        },
        "annotations": {
          "summary": "Docker E2E verification"
        },
        "startsAt": "2026-09-11T10:00:00Z",
        "endsAt": "0001-01-01T00:00:00Z",
        "generatorURL": "http://prometheus:9090/graph",
        "fingerprint": "docker-e2e-test"
      }
    ]
  }'
```

The response contains normalized:

```text
Alert Evidence
Metrics
Health
HTTP Errors
Logs
Timeline
```

---

# Prometheus Verification

Prometheus can be accessed at:

```text
http://localhost:9090
```

## Check Service Availability

```promql
up
```

## Check PostgreSQL Availability

```promql
pg_up
```

## Request Rate

```promql
sum by (job) (
  rate(http_server_requests_seconds_count[5m])
)
```

## P95 Request Latency

```promql
histogram_quantile(
  0.95,
  sum by (job, le) (
    rate(http_server_requests_seconds_bucket[5m])
  )
)
```

## HTTP 5xx Error Rate

```promql
sum by (job) (
  rate(http_server_requests_seconds_count{status=~"5.."}[5m])
)
or on(job)
(
  0 * sum by (job) (
    rate(http_server_requests_seconds_count[5m])
  )
)
```

The `or` expression ensures that healthy services with no recent 5xx responses still appear as:

```text
0
```

rather than:

```text
No data
```

## PostgreSQL Active Connections

```promql
sum by (datname) (
  pg_stat_database_numbackends
)
```

## PostgreSQL Cache Hit Ratio

```promql
100 *
sum by (datname) (
  rate(pg_stat_database_blks_hit[5m])
)
/
(
  sum by (datname) (
    rate(pg_stat_database_blks_hit[5m])
  )
  +
  sum by (datname) (
    rate(pg_stat_database_blks_read[5m])
  )
)
```

## Database Connection Utilization

```promql
(
  sum by (instance) (
    pg_stat_database_numbackends{
      datname!~"template0|template1"
    }
  )
  /
  max by (instance) (
    pg_settings_max_connections
  )
) * 100
```

---

# Alertmanager Verification

Alertmanager can be accessed at:

```text
http://localhost:9093
```

## Check Alertmanager Status

```bash
curl -s http://localhost:9093/api/v2/status
```

## Check Active Alerts

```bash
curl -s http://localhost:9093/api/v2/alerts
```

## Check Prometheus → Alertmanager Connection

```bash
curl -s http://localhost:9090/api/v1/alertmanagers
```

## Check Incident Context Receiver

The active receiver is configured to forward alerts to:

```text
http://incident-context:8090/api/v1/alerts
```

The complete alert pipeline is:

```text
Prometheus
    │
    ▼
Alertmanager
    │
    ▼
Incident Context Builder
```

---

# Docker Compose Verification

The complete platform can be verified with:

```bash
docker compose ps
```

Expected application state:

```text
gateway             healthy
user-service        healthy
order-service       healthy
incident-context    healthy
postgres            healthy
```

The observability services should be running:

```text
prometheus
alertmanager
node-exporter
postgres-exporter
grafana
```

The application can then be tested through the Gateway:

```bash
curl -i http://localhost:8082/api/users
```

and:

```bash
curl -i http://localhost:8082/api/orders
```

The incident pipeline can be tested through:

```bash
curl -sS -X POST http://localhost:8090/api/v1/alerts ...
```

---

# Failure Engineering

A key feature of the project is deliberate failure injection.

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
Incident Evidence
       ↓
AI Incident Analysis
       ↓
Root Cause + Remediation
       ↓
Database Recovery
       ↓
Service Recovery
```

The Order Service has been used for controlled database failure testing.

The Gateway has also been deliberately stopped to validate:

```text
Failure
  ↓
Prometheus Detection
  ↓
Alert Firing
  ↓
Alertmanager Delivery
  ↓
Service Recovery
  ↓
Alert Resolution
```

The Incident Context Builder has additionally been tested against:

```text
Empty alert lists
Missing alert labels
Missing alert annotations
Unknown services
Prometheus unavailability
Malformed structured logs
Missing timestamps
Invalid Alertmanager end timestamps
```

The complete AI-based failure-analysis workflow remains a future stage.

---

# Project Status

## Stage 1 — Project Initialization ✅

* Project structure
* Spring Boot project foundation
* Docker Compose PostgreSQL environment
* Environment configuration
* Initial documentation

## Stage 2 — User Service ✅

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

## Stage 3 — Order Service ✅

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
* Order creation failure handling
* 16/16 automated tests passing

## Stage 4 — API Gateway ✅

* Spring Boot API Gateway
* Centralized client-facing entry point
* User Service routing
* Order Service routing
* Request forwarding using Spring `RestClient`
* Correlation ID generation
* Correlation ID preservation
* Correlation ID propagation to downstream services
* Request-completion logging
* Downstream HTTP error propagation
* Gateway health endpoint
* Configurable User Service URL
* Configurable Order Service URL
* Gateway application-context test
* End-to-end routing verification
* Correlation ID verification
* Request logging verification
* 1/1 automated tests passing

## Stage 5 — Dockerization & Local Platform ✅

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

## Stage 6 — Application Observability ✅

* Spring Boot Actuator integration
* `/actuator/health`
* `/actuator/info`
* `/actuator/metrics`
* `/actuator/prometheus`
* Micrometer instrumentation
* Micrometer Prometheus registry
* HTTP request metrics
* Request duration metrics
* HTTP status metrics
* HTTP method metrics
* HTTP outcome metrics
* HTTP exception/error metrics
* HTTP histogram buckets
* JVM memory metrics
* JVM thread metrics
* Garbage collection metrics
* Process CPU metrics
* System CPU metrics
* Disk metrics
* Executor metrics
* Tomcat metrics
* HikariCP database connection metrics
* JDBC connection metrics
* User Service custom metrics
* `user_creation_total`
* `service_requests_total`
* Order Service custom metrics
* `orders_created_total`
* `orders_failed_total`
* Custom metric unit testing
* Runtime metric verification
* Gateway metrics exposure
* Gateway HTTP metric verification
* Database failure metric verification
* 16/16 Order Service tests passing
* 8/8 User Service tests passing
* 1/1 Gateway tests passing

## Stage 7 — Prometheus Monitoring & Alert Rules ✅

* Prometheus container
* Prometheus persistent storage
* Prometheus scrape configuration
* 15-second scrape interval
* 15-second evaluation interval
* Gateway metrics scraping
* User Service metrics scraping
* Order Service metrics scraping
* Node Exporter integration
* PostgreSQL Exporter integration
* PostgreSQL database metric collection
* Application metric collection
* JVM metric collection
* Infrastructure metric collection
* Prometheus target verification
* Prometheus query verification
* Application alert rules
* Infrastructure alert rules
* Database connection alert
* `ServiceDown`
* `HighHttp5xxErrorRate`
* `HighRequestLatency`
* `HighJvmMemoryUsage`
* `HighCpuUsage`
* `HighMemoryUsage`
* `LowFilesystemSpace`
* `HighSystemLoad`
* `DatabaseConnectionExhaustion`
* Prometheus configuration validation
* Prometheus rule validation
* `promtool` verification
* 9 alert rules successfully loaded

## Stage 8 — Grafana Dashboards ✅

* Grafana container
* Grafana persistent storage
* Prometheus data source
* Automated Grafana data source provisioning
* Automated dashboard provisioning
* Version-controlled dashboard JSON
* Monitoring dashboard folder
* Platform Overview dashboard
* Application Performance dashboard
* JVM dashboard
* Database dashboard
* Service Health dashboard
* 41 operational monitoring panels
* Service availability monitoring
* Request-rate monitoring
* HTTP error-rate monitoring
* P50 latency monitoring
* P95 latency monitoring
* P99 latency monitoring
* HTTP status monitoring
* HTTP method monitoring
* URI monitoring
* JVM heap monitoring
* JVM non-heap monitoring
* JVM thread monitoring
* JVM CPU monitoring
* Garbage collection monitoring
* PostgreSQL availability monitoring
* PostgreSQL connection monitoring
* PostgreSQL database-size monitoring
* PostgreSQL transaction monitoring
* PostgreSQL tuple activity monitoring
* PostgreSQL cache hit monitoring
* PostgreSQL deadlock monitoring
* PostgreSQL temporary-file monitoring
* PostgreSQL background-writer monitoring
* Service-level health dashboards
* Grafana dashboard validation
* All five dashboards visually verified with live data

## Stage 9 — Alertmanager & Incident Alerting ✅

* Alertmanager container
* Alertmanager persistent storage
* Alertmanager configuration
* Prometheus → Alertmanager integration
* Alert routing
* Alert grouping
* Alert lifecycle management
* Alert metadata
* Severity metadata
* Service metadata
* Environment metadata
* Instance metadata
* Alert annotations
* Runbook annotations
* `ServiceDown` alert verification
* Real Gateway failure simulation
* Prometheus firing-alert verification
* Alertmanager active-alert verification
* Gateway recovery verification
* Prometheus alert-resolution verification
* Alertmanager alert-resolution verification
* Alertmanager configuration validation using `amtool`
* Prometheus Alertmanager connection verification
* End-to-end alert lifecycle testing

## Stage 10 — Structured Application Logging ✅

* Structured JSON logging
* Consistent log fields
* Timestamp standardization
* Log level
* Service identification
* Correlation ID propagation
* Correlation ID in logging context
* Request method logging
* Request endpoint logging
* HTTP status logging
* Request duration logging
* Exception-aware structured logging
* Request-completion logging in Gateway
* Request-completion logging in User Service
* Request-completion logging in Order Service
* Machine-readable application log format
* Structured logging verification
* Correlation ID verification
* JSON log verification

## Stage 11 — Incident Data Pipeline ✅

* Dedicated Incident Context Builder service
* Incident Context Builder Docker container
* Incident Context Builder health check
* Alertmanager webhook endpoint
* Alertmanager → Incident Context Builder integration
* Alertmanager payload models
* Alert normalization
* `AlertEvidence`
* `IncidentContext`
* `LogEvidence`
* `HealthEvidence`
* `HttpErrorEvidence`
* `TimelineEvent`
* Prometheus HTTP client
* Prometheus request-rate evidence
* Prometheus HTTP 5xx evidence
* Prometheus HTTP error evidence
* Service health evidence
* User Service health integration
* Order Service health integration
* Structured log parser
* Structured log evidence model
* Deterministic timeline generation
* Alert firing timeline events
* Alert resolved timeline events
* HTTP error timeline events
* Log timeline events
* Chronological event ordering
* Invalid timestamp handling
* Alertmanager placeholder timestamp handling
* Empty-alert resilience
* Missing-label resilience
* Missing-annotation resilience
* Unknown-service resilience
* Prometheus-unavailable resilience
* Best-effort evidence collection
* MockWebServer-based HTTP client tests
* Incident Context Builder integration tests
* Docker E2E verification
* 22/22 Incident Context Builder tests passing
* Alertmanager → Incident Context Builder verification
* Incident Context Builder → Prometheus verification
* Incident Context Builder → Order Service verification
* Incident timeline verification

---

# Current Architecture

The complete local application, monitoring, and incident-data platform now consists of:

```text
                              Client
                                │
                                ▼
                       API Gateway :8082
                                │
                      ┌─────────┴─────────┐
                      │                   │
                      ▼                   ▼
               User Service        Order Service
                  :8080                :8081
                      │                   │
                      └─────────┬─────────┘
                                ▼
                           PostgreSQL
                              :5432
                                │
                                ▼
                       Persistent Storage


                         Observability Layer
                                │
             ┌──────────────────┴──────────────────┐
             │                                     │
             ▼                                     ▼
        Prometheus :9090                       Exporters
             │                               ┌───────────────┐
             │                               │ Node Exporter │
             │                               │ PostgreSQL    │
             │                               │ Exporter     │
             │                               └───────────────┘
             │
        ┌────┴────┐
        │         │
        ▼         ▼
    Grafana   Alertmanager
     :3000       :9093
                    │
                    │ webhook
                    ▼
             Incident Context
                Builder
                 :8090
                    │
          ┌─────────┼──────────┐
          │         │          │
          ▼         ▼          ▼
      Prometheus Health     HTTP Errors
          │         │          │
          └─────────┼──────────┘
                    │
                    ▼
             Structured Logs
                Parser
                    │
                    ▼
             Timeline Builder
                    │
                    ▼
            Incident Context
                    │
                    ▼
           AI Incident Analyzer
                (planned)
```

The current Prometheus monitoring layer collects:

```text
Application Metrics
JVM Metrics
Process Metrics
Infrastructure Metrics
PostgreSQL Metrics
```

The current Grafana layer provides:

```text
Platform Overview
Application Performance
JVM
Database
Service Health
```

with:

```text
41 total monitoring panels
```

The current alerting layer provides:

```text
4 application alerts
5 infrastructure/database alerts
9 total alert rules
```

The current incident-data layer provides:

```text
Alert Evidence
Metric Evidence
Health Evidence
HTTP Error Evidence
Structured Log Parsing
Deterministic Timeline
```

The complete application, monitoring, and incident-data environment can be started reproducibly through:

```bash
docker compose up --build
```

All application containers run as non-root users and participate in a dedicated Docker network.

Health checks and dependency conditions ensure that services start only after their required dependencies are available.

The database uses a persistent Docker volume so application data survives PostgreSQL container restarts.

Prometheus uses persistent storage for collected time-series data.

Alertmanager uses persistent storage for alert-management state.

Grafana dashboards and provisioning configuration are maintained as source-controlled files, making the monitoring environment reproducible.

---

# Roadmap

The current platform has progressed through:

```text
Application Services
        ↓
API Gateway
        ↓
Docker Platform
        ↓
Application Metrics
        ↓
Prometheus
        ↓
Grafana
        ↓
Alertmanager
        ↓
Structured Logging
        ↓
Incident Context Builder
        ↓
Future Centralized Logs
        ↓
Future AI Incident Analyzer
```

The next stages will extend the deterministic incident-data foundation into centralized logging, incident intelligence, failure analysis, and operational reporting.

Planned future capabilities include:

1. Centralized log collection
2. Log aggregation and search
3. Correlation between logs and alerts
4. AI-powered incident analysis
5. Alert correlation
6. Anomaly detection
7. AI-assisted log analysis
8. Automated incident reports
9. Incident history
10. Failure simulation workflows
11. Incident recovery workflows
12. Security hardening
13. CI/CD
14. Production deployment
15. Documentation and portfolio polish

---

# AI Incident Intelligence

The AI layer will analyze the deterministic incident context generated by the Incident Context Builder.

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

The intended architecture is:

```text
Alertmanager
      │
      ▼
Incident Context Builder
      │
      ├── Prometheus Metrics
      ├── Alert Metadata
      ├── Application Health
      ├── HTTP Errors
      ├── Structured Logs
      └── Incident Timeline
              │
              ▼
      AI Incident Analyzer
              │
              ▼
      Root Cause Analysis
              │
              ▼
      Remediation Recommendation
```

The AI system will **not directly execute remediation actions**.

Operational changes will remain under deterministic automation and explicit engineering control.

This separation ensures that AI is used for:

```text
Interpretation
Correlation
Analysis
Recommendation
```

while operational systems remain responsible for:

```text
Execution
State Changes
Remediation
Infrastructure Modification
```

---

# Failure Engineering Roadmap

The long-term failure-analysis workflow is:

```text
Failure Injection
       ↓
Application Impact
       ↓
Metric Changes
       ↓
Prometheus Detection
       ↓
Alertmanager
       ↓
Incident Context Builder
       ↓
Evidence Correlation
       ↓
AI Incident Analyzer
       ↓
Root Cause Analysis
       ↓
Remediation Recommendation
       ↓
Deterministic Automation
       ↓
Recovery
       ↓
Alert Resolution
       ↓
Incident Report
```

The current platform has completed the pipeline through:

```text
Failure
  ↓
Metrics
  ↓
Alert
  ↓
Alertmanager
  ↓
Incident Context
  ↓
Evidence
  ↓
Timeline
```

AI analysis and deterministic remediation remain future stages.

---

# Local Development

## Option 1 — Run the Complete Platform with Docker Compose

From the project root:

```bash
docker compose up --build
```

Verify the containers:

```bash
docker compose ps
```

## Option 2 — Run PostgreSQL with Docker and Applications Locally

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Run User Service:

```bash
cd user-service
./mvnw spring-boot:run
```

Run Order Service:

```bash
cd order-service
./mvnw spring-boot:run
```

Run API Gateway:

```bash
cd gateway
./mvnw spring-boot:run
```

Run Incident Context Builder:

```bash
cd incident-context
./mvnw spring-boot:run
```

The Incident Context Builder starts on:

```text
http://localhost:8090
```

The recommended approach for the complete environment remains Docker Compose:

```bash
docker compose up -d
```

---

# Service Endpoints

| Component                | Port | Endpoint                        |
| ------------------------ | ---: | ------------------------------- |
| API Gateway              | 8082 | `http://localhost:8082`         |
| User Service             | 8080 | `http://localhost:8080`         |
| Order Service            | 8081 | `http://localhost:8081`         |
| Incident Context Builder | 8090 | `http://localhost:8090`         |
| Prometheus               | 9090 | `http://localhost:9090`         |
| Alertmanager             | 9093 | `http://localhost:9093`         |
| Grafana                  | 3000 | `http://localhost:3000`         |
| Node Exporter            | 9100 | `http://localhost:9100/metrics` |
| PostgreSQL Exporter      | 9187 | `http://localhost:9187/metrics` |
| PostgreSQL               | 5432 | `localhost:5432`                |

---

# Portfolio Value

This project demonstrates practical experience across multiple areas of modern backend and DevOps engineering:

```text
Java
Spring Boot
Microservices
REST APIs
PostgreSQL
JPA / Hibernate
Flyway
Docker
Docker Compose
Linux Containers
Non-Root Containers
API Gateway
Distributed Request Context
Structured Logging
Micrometer
Prometheus
Grafana
Alertmanager
Infrastructure Monitoring
Database Monitoring
Incident Data Pipelines
Failure Engineering
Observability
AIOps Foundations
```

The project intentionally separates:

```text
Application Development
        +
Infrastructure
        +
Observability
        +
Incident Intelligence
```

rather than treating monitoring as an isolated dashboarding exercise.

The resulting architecture demonstrates how operational data can flow from distributed applications into metrics, alerts, structured logs, deterministic incident context, and eventually AI-assisted incident analysis.

---

# License

This project is licensed under the [MIT License](LICENSE).

