# Monitoring & Observability Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Production-style microservice monitoring and observability platform with Spring Boot, PostgreSQL, Docker, Micrometer, Prometheus, Grafana, Alertmanager, and AI-powered incident intelligence.

## Overview

This project demonstrates a production-oriented monitoring and observability platform designed around a distributed microservice application.

The platform progressively introduces application services, database persistence, API gateway routing, containerization, health monitoring, application metrics, centralized Prometheus monitoring, Grafana dashboards, alert rules, 
structured logging, incident analysis, and AI-assisted AIOps capabilities.

The system is designed around a clear separation of responsibilities:

* **Applications** generate business traffic, metrics, logs, and health information.
* **API Gateway** provides a centralized client-facing entry point and propagates distributed request context.
* **Container Platform** provides reproducible local deployment, service networking, health checks, and persistent database storage.
* **Observability components** collect, store, visualize, and analyze operational data.
* **Prometheus** collects and stores application and infrastructure metrics and evaluates alerting rules.
* **Grafana** provides centralized operational dashboards for application, JVM, database, and service health monitoring.
* **Alerting components** detect defined failure conditions.
* **AI components** analyze incident context and provide recommendations.
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
                              │ Persistent Docker  │
                              │      Volume        │
                              └────────-- ──────────┘


                     Application / Infrastructure Metrics
                                       │
                                       ▼
                              ┌──────────────────┐
                              │    Prometheus    │
                              │      :9090       │
                              └────────┬─────────┘
                                       │
                          ┌────────────┴────────────┐
                          │                         │
                          ▼                         ▼
                   ┌─────────────┐          ┌──────────────┐
                   │   Grafana   │          │ Alert Rules  │
                   │    :3000    │          │              │
                   └─────────────┘          └──────┬───────┘
                                                   │
                                                   ▼
                                            Alertmanager
                                            (future stage)
                                                   │
                                                   ▼
                                      Incident Context Builder
                                                   │
                                                   ▼
                                         AI Incident Analyzer
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
* HTTP request metrics
* JVM metrics
* Process metrics
* System metrics
* Thread metrics
* Database connection pool metrics
* PostgreSQL database metrics
* Custom application metrics

### AI / AIOps

* AI-powered incident analysis
* Alert correlation
* Anomaly detection
* Log analysis
* Incident summarization
* Root-cause analysis
* Remediation recommendations

The AI/AIOps components are planned for later stages.

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
├── monitoring/
│   ├── prometheus/
│   │   ├── prometheus.yml
│   │   └── rules/
│   │       ├── application.yml
│   │       └── infrastructure.yml
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

The monitoring configuration is intentionally stored as code so that the Prometheus and Grafana environment can be recreated consistently.

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
/api/users/* → http://localhost:8080/users/*
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
/api/orders/* → http://localhost:8081/orders/*
```

The Gateway currently provides:

* Centralized client-facing entry point
* Request routing
* Request forwarding
* Correlation ID generation
* Correlation ID preservation
* Correlation ID propagation
* Basic request-completion logging
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

This establishes the foundation for distributed request observability and structured logging.

---

# Gateway Request Logging

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

---

# Dockerized Local Platform

The complete application and observability stack can now be started through Docker Compose.

The Dockerized platform consists of:

```text
┌──────────────────────────────────────────────────────────┐
│                  Docker Compose Platform                │
│                                                          │
│  ┌──────────────┐                                        │
│  │ API Gateway  │ :8082                                  │
│  └──────┬───────┘                                        │
│         │                                                │
│    ┌────┴────┐                                           │
│    ▼         ▼                                           │
│ ┌───────┐ ┌───────────────┐                              │
│ │ User  │ │    Order      │                              │
│ │ :8080 │ │    :8081      │                              │
│ └───┬───┘ └───────┬───────┘                              │
│     │             │                                      │
│     └──────┬──────┘                                      │
│            ▼                                             │
│     ┌──────────────┐                                     │
│     │  PostgreSQL  │ :5432                               │
│     └──────┬───────┘                                     │
│            │                                             │
│            ▼                                             │
│     postgres-data                                        │
│     persistent volume                                    │
│                                                          │
│  ┌────────────────┐       ┌─────────────────────────┐    │
│  │   Prometheus   │──────►│        Grafana          │    │
│  │     :9090      │       │         :3000           │    │
│  └───────┬────────┘       └─────────────────────────┘    │
│          │                                               │
│         ├──────► Node Exporter                  │
│         │                                              │
│         └──────► PostgreSQL Exporter            │
│                                                         │
└──────────────────────────────────────────────────────────┘
```

## Docker Compose Services

The Compose platform currently contains:

```text
postgres
user-service
order-service
gateway
prometheus
node-exporter
postgres-exporter
grafana
```

All services communicate through the dedicated Docker bridge network:

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

Prometheus → gateway:8082/actuator/prometheus
Prometheus → user-service:8080/actuator/prometheus
Prometheus → order-service:8081/actuator/prometheus
Prometheus → node-exporter:9100
Prometheus → postgres-exporter:9187

Grafana → http://prometheus:9090
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

The application services should report healthy.

The observability services should report as running.

Expected services include:

```text
monitoring-postgres
monitoring-user-service
monitoring-order-service
monitoring-gateway
monitoring-prometheus
monitoring-node-exporter
monitoring-postgres-exporter
monitoring-grafana
```

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

Spring Boot Actuator provides health endpoints for all application services and the Gateway.

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

Example response:

```json
{
  "status": "UP"
}
```

These health endpoints are also used by Docker Compose health checks for the application containers.

They form the foundation for service-health monitoring and failure detection.

---

# Application Observability

## Actuator Endpoints

All three application components expose the following Actuator endpoints:

```text
/actuator/health
/actuator/info
/actuator/metrics
/actuator/prometheus
```

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

The counter is incremented after a user has been successfully persisted.

### Service Request Counter

```text
service_requests_total
```

Description:

```text
Total number of requests handled by the User Service
```

The counter is incremented for User Service controller requests.

## Order Service Metrics

### Successful Order Creation Counter

```text
orders_created_total
```

Description:

```text
Total number of orders successfully created
```

The counter is incremented after an order has been successfully persisted.

### Failed Order Creation Counter

```text
orders_failed_total
```

Description:

```text
Total number of failed order creation attempts
```

The counter is incremented when order creation fails during persistence.

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
Maximum request time
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

In addition, the platform now uses **PostgreSQL Exporter** to expose PostgreSQL server-level database metrics.

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

## Target Verification

Prometheus target health was verified for:

```text
gateway
user-service
order-service
node-exporter
postgres
```

Application target health is represented by:

```promql
up
```

PostgreSQL availability is represented by:

```promql
pg_up
```

---

# Prometheus Alert Rules

Prometheus currently evaluates application and infrastructure alert rules.

The rules are maintained as source-controlled files:

```text
monitoring/prometheus/rules/application.yml
monitoring/prometheus/rules/infrastructure.yml
```

## Application Alerts

### ServiceDown

Detects an unavailable Prometheus target.

```text
up == 0
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

The current threshold reflects the four-CPU Linux environment used by the Dockerized monitoring stack.

> **Note:** Node Exporter is currently running inside the Docker environment on macOS. Its infrastructure metrics therefore represent the Linux environment available to the containerized stack rather than the physical Mac host's 
hardware resources.

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

Grafana dashboard provisioning is defined in:

```text
monitoring/grafana/provisioning/
```

Dashboard JSON definitions are stored in:

```text
monitoring/grafana/dashboards/
```

The current implementation contains **5 dashboards with 41 panels**.

---

## Dashboard 1 — Platform Overview

The Platform Overview dashboard provides a high-level operational view of the entire platform.

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

The dashboard provides an immediate answer to:

```text
Are the core services available?
Is traffic flowing?
Are requests failing?
Is request latency increasing?
```

---

## Dashboard 2 — Application Performance

The Application Performance dashboard focuses on HTTP-level application behavior.

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

This dashboard provides visibility into:

* Application throughput
* Request latency
* HTTP response distribution
* Request methods
* Frequently accessed endpoints

---

## Dashboard 3 — JVM

The JVM dashboard focuses on runtime health across the three Spring Boot services.

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

The dashboard provides visibility into:

* Heap pressure
* JVM memory allocation
* Non-heap memory
* Garbage collection activity
* Thread count
* Application CPU utilization

---

## Dashboard 4 — Database

The Database dashboard focuses on PostgreSQL operational health and activity.

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

The dashboard provides visibility into:

* PostgreSQL availability
* Connection utilization
* Database growth
* Transaction activity
* Read activity
* Write activity
* Cache effectiveness
* Deadlocks
* Temporary file generation
* Background writer behavior

---

## Dashboard 5 — Service Health

The Service Health dashboard provides an operational view of individual service availability and service-level behavior.

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

The dashboard makes it possible to quickly identify:

```text
Which service is unavailable?
Is PostgreSQL healthy?
Which service is receiving traffic?
Are services producing 5xx errors?
Which service has elevated latency?
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

The data source is configured as the default Grafana data source.

## Dashboard Provider

Dashboard provisioning is configured using:

```text
monitoring/grafana/provisioning/dashboards/dashboards.yml
```

The provider loads dashboard definitions from:

```text
/var/lib/grafana/dashboards
```

The Docker Compose configuration mounts the repository directory:

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

indicating a healthy PostgreSQL connection.

PostgreSQL activity metrics such as:

```text
pg_stat_database_numbackends
pg_stat_database_xact_commit
pg_stat_database_blks_hit
pg_stat_database_blks_read
pg_stat_database_tup_fetched
```

were also successfully queried through Prometheus.

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
4 infrastructure alert rules
```

for a total of:

```text
8 alert rules
```

The Prometheus configuration and rule files were validated using `promtool`.

## Grafana

All five dashboards were successfully provisioned into Grafana.

The dashboards were visually validated and all panels returned data.

Current dashboard inventory:

```text
Platform Overview       9 panels
Application Performance 7 panels
JVM                     7 panels
Database               10 panels
Service Health           8 panels
──────────────────────────────
Total                   41 panels
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

The `500` and `503` measurements were generated during deliberate database outage testing and demonstrate that failed requests are visible through the HTTP metrics pipeline.

The custom metrics:

```text
orders_created_total
orders_failed_total
```

were registered and verified.

A successful order creation incremented:

```text
orders_created_total
```

A simulated persistence failure was handled by the service and verified through the failure counter unit test.

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

and additional Spring Boot runtime metrics.

Gateway HTTP metrics were verified using:

```bash
curl -s http://localhost:8082/actuator/metrics/http.server.requests
```

After generating:

```bash
curl -s http://localhost:8082/api/users
```

the Gateway metrics included:

```text
/api/users
```

as a tracked URI.

The metric captured:

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

This confirms that the Gateway is observable as a first-class component of the platform rather than merely acting as a routing layer.

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

# Application Logging

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

These logs establish the foundation for the structured logging and centralized incident-analysis pipeline.

Full JSON-based structured logging is intentionally deferred to the dedicated logging stage.

---

# Testing

The platform currently contains automated tests for the User Service, Order Service, and API Gateway.

## Order Service

The current Order Service test suite contains:

```text
16 tests
16 passed
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
* Successful order creation metric
* Failed order creation metric

Run the Order Service tests with:

```bash
cd order-service
./mvnw clean test
```

## User Service

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

The User Service tests cover the service's application context and API behavior, including the service functionality introduced through the observability implementation.

## API Gateway

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
* Gateway Actuator metrics
* Gateway HTTP request metrics

---

# Dockerized End-to-End Verification

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

The observability pipeline was verified independently:

```text
User Service ──────┐
Order Service ─────┤
Gateway ───────────┼──► Prometheus ───► Grafana
                   │
Node Exporter ─────┤
                   │
PostgreSQL Exporter┘
```

Prometheus successfully collected application, JVM, infrastructure, and PostgreSQL metrics.

Grafana successfully queried Prometheus and displayed the collected data across all five dashboards.

---

# Local Development

## Option 1 — Run the Complete Platform with Docker Compose

From the project root:

```bash
docker compose up --build
```

This is the recommended way to run the complete local application and observability platform.

Verify the containers:

```bash
docker compose ps
```

## Option 2 — Run PostgreSQL with Docker and Applications Locally

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

## Run User Service

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

## Run Order Service

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

## Run API Gateway

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

## Run Prometheus and Grafana

The recommended approach is to run the complete Compose stack:

```bash
docker compose up -d
```

Prometheus:

```text
http://localhost:9090
```

Grafana:

```text
http://localhost:3000
```

PostgreSQL Exporter:

```text
http://localhost:9187/metrics
```

Node Exporter:

```text
http://localhost:9100/metrics
```

The recommended client flow remains:

```text
Client
  ↓
API Gateway :8082
  ↓
User Service :8080
       OR
Order Service :8081
```

The observability flow is:

```text
Applications
     │
     ▼
Micrometer
     │
     ▼
/actuator/prometheus
     │
     ▼
Prometheus :9090
     │
     ▼
Grafana :3000
```

---

# User Service API Examples

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

## Metrics

```bash
curl http://localhost:8080/actuator/metrics
```

## Prometheus Metrics

```bash
curl http://localhost:8080/actuator/prometheus
```

## HTTP Request Metrics

```bash
curl http://localhost:8080/actuator/metrics/http.server.requests
```

## Create a User

```bash
curl -i -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com"
  }'
```

## Get a User

```bash
curl -i http://localhost:8080/users/1
```

## Get All Users

```bash
curl -i http://localhost:8080/users
```

## Delete a User

```bash
curl -i -X DELETE http://localhost:8080/users/1
```

---

# Order Service API Examples

## Health Check

```bash
curl http://localhost:8081/actuator/health
```

## Metrics

```bash
curl http://localhost:8081/actuator/metrics
```

## Prometheus Metrics

```bash
curl http://localhost:8081/actuator/prometheus
```

## HTTP Request Metrics

```bash
curl http://localhost:8081/actuator/metrics/http.server.requests
```

## Create an Order

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

## Get an Order

```bash
curl -i http://localhost:8081/orders/1
```

## Get All Orders

```bash
curl -i http://localhost:8081/orders
```

## Update an Order

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

## Delete an Order

```bash
curl -i -X DELETE http://localhost:8081/orders/1
```

---

# API Gateway Examples

The Gateway provides the preferred client-facing API.

## Gateway Health Check

```bash
curl -i http://localhost:8082/actuator/health
```

## Gateway Metrics

```bash
curl -i http://localhost:8082/actuator/metrics
```

## Gateway Prometheus Metrics

```bash
curl -i http://localhost:8082/actuator/prometheus
```

## Gateway HTTP Request Metrics

```bash
curl -i http://localhost:8082/actuator/metrics/http.server.requests
```

## Create a User Through Gateway

```bash
curl -i -X POST http://localhost:8082/api/users \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: user-request-001" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com"
  }'
```

## Get a User Through Gateway

```bash
curl -i http://localhost:8082/api/users/1 \
  -H "X-Correlation-ID: user-request-002"
```

## Get All Users Through Gateway

```bash
curl -i http://localhost:8082/api/users \
  -H "X-Correlation-ID: user-request-003"
```

## Delete a User Through Gateway

```bash
curl -i -X DELETE http://localhost:8082/api/users/1 \
  -H "X-Correlation-ID: user-request-004"
```

## Create an Order Through Gateway

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

## Get an Order Through Gateway

```bash
curl -i http://localhost:8082/api/orders/1 \
  -H "X-Correlation-ID: order-request-002"
```

## Get All Orders Through Gateway

```bash
curl -i http://localhost:8082/api/orders \
  -H "X-Correlation-ID: order-request-003"
```

## Update an Order Through Gateway

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

## Delete an Order Through Gateway

```bash
curl -i -X DELETE http://localhost:8082/api/orders/1 \
  -H "X-Correlation-ID: order-request-005"
```

## Verify Correlation ID

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

---

# Grafana Verification

Grafana can be accessed at:

```text
http://localhost:3000
```

The current dashboard folder is:

```text
Monitoring
```

Available dashboards:

```text
Platform Overview
Application Performance
JVM
Database
Service Health
```

Grafana automatically provisions the dashboards from:

```text
monitoring/grafana/dashboards/
```

and the Prometheus data source from:

```text
monitoring/grafana/provisioning/datasources/prometheus.yml
```

---

# Docker Compose Verification

The complete platform can be verified with:

```bash
docker compose ps
```

Expected application state:

```text
gateway          healthy
user-service     healthy
order-service    healthy
postgres         healthy
```

The observability services should be running:

```text
prometheus       running
node-exporter    running
postgres-exporter running
grafana          running
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

The monitoring pipeline can then be verified through:

```text
Prometheus :9090
Grafana    :3000
```

---

# Observability Roadmap

The platform will progressively evolve from application-level instrumentation into a complete monitoring and AIOps platform.

The current observability pipeline is:

```text
Applications
    │
    ├── Metrics ──────────► Prometheus
    │                         │
    │                         ▼
    │                      Grafana
    │
    ├── Health ───────────► Prometheus
    │
    └── Infrastructure ──► Exporters
                              │
                              ▼
                          Prometheus
```

The planned future incident-intelligence pipeline is:

```text
Applications
    │
    ├── Metrics ──────────► Prometheus
    │                         │
    │                         ▼
    │                      Grafana
    │
    ├── Alerts ───────────► Alertmanager
    │                         │
    ├── Logs ─────────────► Incident Context
    │                         Builder
    │
    └── Health ───────────► Incident Context
                              Builder
                                  │
                                  ▼
                         AI Incident Analyzer
```

Planned future stages include:

1. Alertmanager integration
2. Structured application logging
3. Centralized log collection
4. Incident context generation
5. AI-powered incident analysis
6. Alert correlation
7. Anomaly detection
8. AI log analysis
9. Incident timeline generation
10. Automated incident reports
11. Failure simulation
12. Incident recovery workflows
13. Incident history
14. Testing and reliability validation
15. Security hardening
16. CI/CD
17. Documentation and portfolio polish

---

# AI Incident Intelligence

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

# Failure Engineering

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

The Order Service has already been used for controlled database failure testing during the application observability stage.

The full automated failure-detection and AI-analysis workflow will be implemented in later stages.

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

## Stage 7 — Prometheus Monitoring & Alerting ✅

* Prometheus container
* Prometheus persistent storage
* Prometheus scrape configuration
* 15-second scrape interval
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
* `ServiceDown`
* `HighHttp5xxErrorRate`
* `HighRequestLatency`
* `HighJvmMemoryUsage`
* `HighCpuUsage`
* `HighMemoryUsage`
* `LowFilesystemSpace`
* `HighSystemLoad`
* Prometheus configuration validation
* Prometheus rule validation
* `promtool` verification
* 8 alert rules successfully loaded

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

---

# Current Architecture

The complete local application and observability platform now consists of:

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


             ┌────────────────────────────────┐
             │       Observability Layer      │
             │                                │
             │  ┌──────────────────────────┐  │
             │  │       Prometheus         │  │
             │  │          :9090            │  │
             │  └────────────┬─────────────┘  │
             │               │                │
             │               ▼                │
             │  ┌──────────────────────────┐  │
             │  │         Grafana           │  │
             │  │          :3000            │  │
             │  └──────────────────────────┘  │
             │                                │
             │  Node Exporter :9100           │
             │  PostgreSQL Exporter :9187     │
             └────────────────────────────────┘
```

The application components expose operational metrics through:

```text
Spring Boot Actuator
        │
        ▼
     Micrometer
        │
        ▼
/actuator/prometheus
        │
        ▼
    Prometheus
        │
        ▼
     Grafana
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

The complete application and monitoring environment can now be started reproducibly through:

```bash
docker compose up --build
```

All application containers run as non-root users and participate in a dedicated Docker network.

Health checks and dependency conditions ensure that services start only after their required dependencies are available.

The database uses a persistent Docker volume so application data survives PostgreSQL container restarts.

Prometheus uses persistent storage for collected time-series data.

Grafana dashboards and provisioning configuration are maintained as source-controlled files, making the monitoring environment reproducible.

---

# Next Stage

**Stage 9 — Alertmanager & Incident Notification** 🚧

The next stage will extend the current Prometheus alerting foundation into a complete alert-delivery pipeline.

Planned responsibilities include:

* Alertmanager container
* Prometheus → Alertmanager integration
* Alert routing
* Severity-based routing
* Alert grouping
* Alert deduplication
* Alert inhibition
* Notification configuration
* Alert lifecycle verification
* Failure simulation
* End-to-end alert delivery testing

The resulting flow will become:

```text
Application / Infrastructure
           │
           ▼
       Prometheus
           │
           │ Alert
           ▼
      Alertmanager
           │
           ▼
    Notification Channel
```

Later stages will extend this into:

```text
Alertmanager
      │
      ▼
Incident Context Builder
      │
      ├── Prometheus Metrics
      ├── Application Logs
      ├── Service Health
      └── Alert Metadata
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

---

# License

This project is licensed under the [MIT License](LICENSE).

