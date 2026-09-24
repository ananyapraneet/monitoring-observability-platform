# Monitoring & Observability Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A production-style microservice monitoring and observability platform built with **Java, Spring Boot, PostgreSQL, Docker, Prometheus, Grafana, Alertmanager, structured logging, deterministic incident context generation, and 
evidence-backed incident analysis**.

The project demonstrates how operational signals from distributed applications can be collected, normalized, correlated, and transformed into structured incident analysis without giving an AI component direct access to 
infrastructure or application state.

---

## Overview

Modern distributed systems generate operational signals across multiple layers:

```text
Application
    ↓
HTTP Requests
    ↓
Metrics
    ↓
Logs
    ↓
Health Signals
    ↓
Alerts
    ↓
Incident Context
    ↓
Evidence Correlation
    ↓
Incident Analysis
```

This project implements that pipeline as a set of independently deployable services and observability components.

The platform currently includes:

* Two Spring Boot business services
* A Spring Boot API Gateway
* PostgreSQL persistence
* Docker Compose-based local infrastructure
* Micrometer application instrumentation
* Prometheus metrics collection
* Grafana operational dashboards
* Alertmanager alert routing and grouping
* Node Exporter infrastructure metrics
* PostgreSQL Exporter database metrics
* Structured JSON application logging
* A dedicated Incident Context Builder
* A deterministic multi-signal correlation engine
* A dedicated AI Incident Analyzer
* Evidence-backed incident analysis
* Confidence scoring
* Recommended remediation
* Docker end-to-end verification
* Automated test coverage across the platform

The AI Incident Analyzer is intentionally implemented as a **deterministic, read-only decision-support system**.

It does not directly:

* modify infrastructure
* restart containers
* modify databases
* execute shell commands
* change application configuration
* scale services

Operational changes remain under explicit engineering control.

---

# Architecture

The platform is organized into four major layers:

```text
┌─────────────────────────────────────────────────────────────┐
│                     Application Layer                       │
│                                                             │
│  Client → API Gateway → User Service / Order Service        │
│                              │              │               │
│                              └──────┬───────┘               │
│                                     ▼                       │
│                                 PostgreSQL                  │
└─────────────────────────────────────────────────────────────┘
                                      │
                                      │ metrics / logs / health
                                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    Observability Layer                      │
│                                                             │
│  Micrometer → Prometheus → Grafana                          │
│                       │                                     │
│                       └────→ Alert Rules                    │
│                                  │                          │
│                                  ▼                          │
│                             Alertmanager                    │
└─────────────────────────────────────────────────────────────┘
                                      │
                                      │ webhook
                                      ▼
┌─────────────────────────────────────────────────────────────┐
│                   Incident Intelligence                     │
│                                                             │
│  Incident Context Builder :8090                             │
│       │                                                     │
│       ├── Alert evidence                                    │
│       ├── Prometheus metrics                                │
│       ├── Service health                                    │
│       ├── HTTP error evidence                               │
│       ├── Structured log parsing                            │
│       └── Deterministic timeline                            │
│                       │                                     │
│                       ▼                                     │
│              Normalized IncidentContext                     │
│                       │                                     │
│                       ▼                                     │
│        AI Incident Analyzer :8091                           │
│                       │                                     │
│                       ├── Alert grouping                    │
│                       ├── Temporal correlation              │
│                       ├── Service dependency correlation    │
│                       ├── Metric correlation                │
│                       ├── Log correlation                   │
│                       └── Multi-signal correlation          │
│                       │                                     │
│                       ▼                                     │
│              Evidence-backed Analysis                       │
└─────────────────────────────────────────────────────────────┘
```

## Application Architecture

```text
                         ┌─────────────────┐
                         │     Client      │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │   API Gateway   │
                         │     :8082       │
                         └───────┬─┬───────┘
                                 │ │
                    ┌────────────┘ └────────────┐
                    ▼                           ▼
             ┌──────────────┐           ┌──────────────┐
             │ User Service │           │ Order Service│
             │    :8080     │           │    :8081     │
             └──────┬───────┘           └──────┬───────┘
                    │                          │
                    └────────────┬─────────────┘
                                 ▼
                         ┌──────────────┐
                         │  PostgreSQL  │
                         │    :5432     │
                         └──────────────┘
```

The business services are independently deployable Spring Boot applications.

Both services use the same PostgreSQL instance during local development while maintaining separate application schemas and Flyway migration histories.

---

# Observability Architecture

```text
                    Application Services
                            │
                            │ Micrometer
                            ▼
                      ┌─────────────┐
                      │ Prometheus  │
                      │    :9090    │
                      └──────┬──────┘
                             │
              ┌──────────────┼───────────────┐
              │              │               │
              ▼              ▼               ▼
         ┌─────────┐   ┌────────────┐   ┌──────────────┐
         │ Grafana │   │Alert Rules │   │ Alertmanager │
         │  :3000  │   │            │   │    :9093     │
         └─────────┘   └────────────┘   └──────┬───────┘
                                               │
                                               │ webhook
                                               ▼
                                      ┌──────────────────┐
                                      │ Incident Context │
                                      │ Builder :8090    │
                                      └────────┬─────────┘
                                               │
                         ┌─────────────────────┼────────────────────┐
                         │                     │                    │
                         ▼                     ▼                    ▼
                    Prometheus          Service Health       HTTP Errors
                         │                     │                    │
                         └─────────────────────┼────────────────────┘
                                               │
                                               ▼
                                      Structured Log Parser
                                               │
                                               ▼
                                        Timeline Builder
                                               │
                                               ▼
                                      IncidentContext JSON
```

The observability layer collects:

* Application metrics
* JVM metrics
* Process metrics
* HTTP metrics
* Database connection metrics
* PostgreSQL server metrics
* Infrastructure metrics
* Structured application logs
* Application health information
* Alert metadata

---

# Incident Intelligence Architecture

Stage 13 extends the original incident-analysis pipeline with explicit evidence correlation.

```text
                    Normalized IncidentContext
                              │
                              ▼
              ┌──────────────────────────────┐
              │ Incident Context Correlation │
              │          Adapter             │
              └──────────────┬───────────────┘
                             │
                             ▼
              ┌──────────────────────────────┐
              │ Multi-Signal Correlation     │
              │           Engine             │
              └──────────────┬───────────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
          ▼                  ▼                  ▼
     Temporal          Dependency           Metric
    Correlation        Correlation        Correlation
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
                             ▼
                       Log Correlation
                             │
                             ▼
                 MultiSignalCorrelation
                             │
                             ▼
                  CorrelatedIncident
                             │
                             ▼
              Rule-Based Incident Analyzer
                             │
                  ┌──────────┴──────────┐
                  │                     │
                  ▼                     ▼
           Confidence Scorer       Analysis Rules
                  │                     │
                  └──────────┬──────────┘
                             ▼
                    IncidentAnalysis
```

The correlation layer intentionally treats relationships as **evidence rather than automatic proof of causation**.

For example:

```text
Alert A
   │
   │ occurs 30 seconds before
   ▼
Alert B
```

is temporal evidence.

It does not automatically mean:

```text
Alert A caused Alert B
```

Similarly, a service dependency indicates an architectural relationship, not necessarily a confirmed root cause.

---

# Technology Stack

## Application

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

## API Gateway

* Spring Boot
* Spring Web
* Spring `RestClient`
* Servlet Filters
* Correlation ID propagation
* Request-completion logging
* Downstream error propagation
* Actuator
* Micrometer

## Containerization

* Docker
* Docker Compose
* Multi-stage Docker builds
* Eclipse Temurin 17 JRE
* Non-root application containers
* Docker health checks
* Docker bridge networking
* Persistent Docker volumes

## Observability

* Prometheus
* Grafana
* Alertmanager
* Node Exporter
* PostgreSQL Exporter
* Micrometer
* Spring Boot Actuator
* Prometheus alert rules
* Grafana provisioning
* Structured JSON logging
* Correlation IDs

## Incident Intelligence

* Alertmanager webhook ingestion
* Alert normalization
* Incident context generation
* Prometheus evidence collection
* Service health evidence
* HTTP error evidence
* Structured log parsing
* Deterministic incident timelines
* Alert grouping
* Temporal correlation
* Service dependency correlation
* Metric correlation
* Log correlation
* Multi-signal correlation
* Deterministic rule-based reasoning
* Confidence scoring
* Evidence-backed incident analysis
* Recommended remediation
* Future LLM integration boundary

---

# Project Structure

```text
monitoring-observability-platform/
│
├── gateway/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/ananyapraneet/monitoring/gateway/
│   │   │   │       ├── config/
│   │   │   │       ├── controller/
│   │   │   │       ├── exception/
│   │   │   │       ├── filter/
│   │   │   │       └── GatewayApplication.java
│   │   │   └── resources/
│   │   │       └── application.yaml
│   │   ├── test/
│   │   └── ...
│   ├── Dockerfile
│   └── pom.xml
│
├── user-service/
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
│   │   │       ├── db/migration/
│   │   │       └── application.yml
│   │   ├── test/
│   │   └── ...
│   ├── Dockerfile
│   └── pom.xml
│
├── order-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/ananyapraneet/monitoring/orderservice/
│   │   │   │       ├── controller/
│   │   │   │       ├── dto/
│   │   │   │       ├── entity/
│   │   │   │       ├── exception/
│   │   │   │       ├── repository/
│   │   │   │       ├── service/
│   │   │   │       └── OrderServiceApplication.java
│   │   │   └── resources/
│   │   │       ├── db/migration/
│   │   │       └── application.yaml
│   │   ├── test/
│   │   └── ...
│   ├── Dockerfile
│   └── pom.xml
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
│   │   └── ...
│   ├── Dockerfile
│   └── pom.xml
│
├── ai-incident-analyzer/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/ananyapraneet/monitoring/aiincidentanalyzer/
│   │   │   │       ├── client/
│   │   │   │       │   └── model/
│   │   │   │       ├── controller/
│   │   │   │       ├── domain/
│   │   │   │       └── service/
│   │   │   │           ├── analysis/
│   │   │   │           └── correlation/
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   ├── test/
│   │   ├── Dockerfile
│   │   └── pom.xml
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
│       └── dashboards/
│           ├── platform-overview.json
│           ├── application-performance.json
│           ├── jvm.json
│           ├── database.json
│           └── service-health.json
│
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

---

# Business Services

## User Service

The User Service provides:

```text
POST   /users
GET    /users
GET    /users/{id}
DELETE /users/{id}
```

User model:

```text
id
name
email
createdAt
updatedAt
```

Capabilities include:

* REST API
* DTO validation
* PostgreSQL persistence
* Spring Data JPA
* Flyway migrations
* Duplicate email protection
* Global exception handling
* Transaction management
* Actuator
* Micrometer
* Prometheus metrics
* Custom application metrics
* Structured JSON logging
* Correlation ID support
* Docker health checks
* Non-root container execution

Runs on:

```text
http://localhost:8080
```

---

## Order Service

The Order Service provides:

```text
POST   /orders
GET    /orders
GET    /orders/{id}
PUT    /orders/{id}
DELETE /orders/{id}
```

Order model:

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

Supported lifecycle states:

```text
CREATED
PROCESSING
COMPLETED
CANCELLED
```

Capabilities include:

* REST API
* DTO validation
* PostgreSQL persistence
* Dedicated `order_service` schema
* Flyway migrations
* Hibernate schema validation
* Global exception handling
* Transaction management
* Structured JSON logging
* Correlation ID support
* Actuator
* Micrometer
* Prometheus metrics
* Custom business metrics
* Docker health checks
* Non-root container execution

Runs on:

```text
http://localhost:8081
```

---

# API Gateway

The API Gateway provides the centralized client-facing entry point.

Runs on:

```text
http://localhost:8082
```

## Routes

```text
/api/users/*  → http://user-service:8080/users/*
/api/orders/* → http://order-service:8081/orders/*
```

Capabilities include:

* Centralized routing
* Request forwarding
* Correlation ID generation
* Correlation ID preservation
* Correlation ID propagation
* Request-completion logging
* Downstream HTTP error propagation
* Actuator
* Micrometer
* Prometheus metrics
* Configurable downstream service URLs
* Docker health checks

---

# Distributed Correlation IDs

The platform uses:

```text
X-Correlation-ID
```

to associate requests across services.

If a client provides a correlation ID, the Gateway preserves it.

Otherwise, the Gateway generates a UUID.

The ID is propagated to downstream services and placed into the application logging context.

```text
Client
   │
   │ X-Correlation-ID
   ▼
API Gateway
   │
   │ X-Correlation-ID
   ▼
Order Service
   │
   ▼
Structured Application Log
```

This provides the foundation for distributed request tracing through logs and incident evidence.

---

# Structured Logging

The Gateway, User Service, and Order Service produce structured JSON request logs.

Typical fields include:

```text
timestamp
level
requestId
service
logger
message
```

Request-completion logs additionally capture:

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

The structured log format is designed for machine processing and future centralized log ingestion.

---

# Dockerized Platform

The complete platform is deployed locally through Docker Compose.

Services:

```text
postgres
user-service
order-service
gateway
incident-context
ai-incident-analyzer
prometheus
alertmanager
node-exporter
postgres-exporter
grafana
```

All services communicate through:

```text
monitoring-network
```

## Internal Communication

```text
Gateway
 ├── user-service:8080
 └── order-service:8081

User Service
 └── postgres:5432

Order Service
 └── postgres:5432

Prometheus
 ├── gateway:8082/actuator/prometheus
 ├── user-service:8080/actuator/prometheus
 ├── order-service:8081/actuator/prometheus
 ├── node-exporter:9100
 └── postgres-exporter:9187

Grafana
 └── prometheus:9090

Prometheus
 └── alertmanager:9093

Alertmanager
 └── incident-context:8090/api/v1/alerts

Incident Context Builder
 ├── prometheus:9090
 ├── user-service:8080
 └── order-service:8081

AI Incident Analyzer
 └── incident-context:8090
```

---

# Start the Platform

From the repository root:

```bash
docker compose up --build
```

Or run in detached mode:

```bash
docker compose up -d --build
```

Verify containers:

```bash
docker compose ps
```

---

# Container Health

Core application services expose:

```text
/actuator/health
```

PostgreSQL uses:

```text
pg_isready
```

Docker Compose dependency conditions are used so that dependent application services wait for required dependencies to become healthy.

The intended dependency chain is:

```text
PostgreSQL
    │
    ├──► User Service
    │
    └──► Order Service
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
    │
    ▼
AI Incident Analyzer
```

---

# Non-Root Containers

Application containers run using a dedicated Linux user:

```text
appuser
UID: 10001
```

Application images use multi-stage builds:

```text
Maven Builder
     ↓
Compiled JAR
     ↓
Eclipse Temurin 17 JRE
```

The final runtime image does not contain Maven build tooling.

This keeps the runtime environment smaller and avoids running application processes as root.

---

# Persistent Storage

## PostgreSQL

Named volume:

```text
monitoring-observability-platform_postgres-data
```

Mounted at:

```text
/var/lib/postgresql/data
```

## Prometheus

Named volume:

```text
prometheus-data
```

Mounted at:

```text
/prometheus
```

## Grafana

Named volume:

```text
grafana-data
```

Mounted at:

```text
/var/lib/grafana
```

Dashboard definitions remain version-controlled under:

```text
monitoring/grafana/dashboards/
```

## Alertmanager

Named volume:

```text
alertmanager-data
```

Mounted at:

```text
/alertmanager
```

---

# Database

PostgreSQL is the platform's relational persistence layer.

It runs on:

```text
localhost:5432
```

## User Service Schema

The User Service uses the PostgreSQL `public` schema.

Migration:

```text
V1__create_users_table.sql
```

## Order Service Schema

The Order Service uses:

```text
order_service
```

Migration:

```text
V1__create_orders_table.sql
```

Hibernate is configured to validate the schema rather than modify it automatically.

Flyway is responsible for schema evolution.

---

# Application Observability

Spring Boot Actuator and Micrometer provide application instrumentation.

Exposed endpoints include:

```text
/actuator/health
/actuator/info
/actuator/metrics
/actuator/prometheus
```

Prometheus consumes:

```text
/actuator/prometheus
```

---

# Metrics

The platform collects standard operational metrics including:

* HTTP request rate
* Request duration
* HTTP status codes
* HTTP methods
* Request outcomes
* Exceptions
* JVM memory
* JVM threads
* Garbage collection
* Process CPU
* System CPU
* Disk usage
* Executor activity
* Tomcat activity
* HikariCP connection pools
* JDBC activity

The primary HTTP metric is:

```text
http.server.requests
```

which is exposed to Prometheus as:

```text
http_server_requests_seconds_count
http_server_requests_seconds_sum
http_server_requests_seconds_bucket
http_server_requests_seconds_max
```

Histogram buckets allow calculation of:

```text
P50
P95
P99
```

latency.

---

# Custom Application Metrics

## User Service

```text
user_creation_total
service_requests_total
```

## Order Service

```text
orders_created_total
orders_failed_total
```

These metrics provide business-level signals in addition to standard framework metrics.

---

# PostgreSQL Monitoring

PostgreSQL Exporter exposes database-level metrics through:

```text
postgres-exporter:9187
```

Important metrics include:

```text
pg_up
pg_database_size_bytes
pg_settings_max_connections
pg_stat_database_numbackends
pg_stat_database_xact_commit
pg_stat_database_xact_rollback
pg_stat_database_blks_hit
pg_stat_database_blks_read
pg_stat_database_deadlocks
pg_stat_database_temp_files
pg_stat_database_temp_bytes
```

These provide visibility into:

* PostgreSQL availability
* Active connections
* Database size
* Transactions
* Cache behavior
* Deadlocks
* Temporary file activity

---

# Prometheus

Prometheus runs on:

```text
http://localhost:9090
```

Configuration:

```text
scrape interval: 15s
evaluation interval: 15s
```

Scrape targets:

```text
gateway
user-service
order-service
node-exporter
postgres-exporter
```

Prometheus stores collected time-series data in persistent storage.

---

# Prometheus Alert Rules

The platform contains:

```text
4 application alerts
5 infrastructure/database alerts
────────────────────────────
9 total alert rules
```

## Application Alerts

### ServiceDown

Detects unavailable application Prometheus targets.

```promql
up{job=~"gateway|user-service|order-service"} == 0
```

### HighHttp5xxErrorRate

Detects elevated HTTP 5xx traffic.

Threshold:

```text
> 5%
for 5 minutes
```

### HighRequestLatency

Detects elevated P95 request latency.

Threshold:

```text
> 1 second
for 5 minutes
```

### HighJvmMemoryUsage

Detects high JVM heap utilization.

Threshold:

```text
> 85%
for 5 minutes
```

## Infrastructure / Database Alerts

```text
HighCpuUsage
HighMemoryUsage
LowFilesystemSpace
HighSystemLoad
DatabaseConnectionExhaustion
```

Thresholds and durations are defined in the version-controlled Prometheus rule files.

Node Exporter currently runs inside the Docker environment on macOS, so its infrastructure metrics represent the Linux environment available to the container rather than the physical Mac host.

---

# Alertmanager

Alertmanager runs on:

```text
http://localhost:9093
```

Prometheus sends firing alerts to Alertmanager.

Alertmanager handles:

* Alert grouping
* Alert routing
* Alert lifecycle
* Repeat notifications

The active receiver forwards alerts to:

```text
http://incident-context:8090/api/v1/alerts
```

Pipeline:

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

The Alertmanager configuration is maintained at:

```text
monitoring/alertmanager/alertmanager.yml
```

It has been validated with `amtool`.

---

# Grafana

Grafana runs on:

```text
http://localhost:3000
```

Prometheus is provisioned as the Grafana data source.

The project contains:

```text
5 dashboards
41 monitoring panels
```

## Dashboards

### Platform Overview

9 panels covering:

* Service availability
* Request rate
* HTTP 5xx rate
* P50 latency
* P95 latency
* P99 latency

### Application Performance

7 panels covering:

* Requests/second
* Latency
* HTTP status distribution
* HTTP methods
* Top request URIs

### JVM

7 panels covering:

* Heap usage
* Heap used
* Heap max
* Non-heap usage
* Garbage collection
* Live threads
* CPU

### Database

10 panels covering:

* PostgreSQL availability
* Active connections
* Database size
* Transactions
* Tuple activity
* Cache hit ratio
* Deadlocks
* Temporary files
* Background writer activity

### Service Health

8 panels covering:

* Gateway health
* User Service health
* Order Service health
* PostgreSQL health
* Overall availability
* Request rate
* Error rate
* P95 latency

Dashboard JSON definitions and provisioning configuration are stored in Git.

---

# Incident Context Builder

The Incident Context Builder is a dedicated service responsible for collecting and normalizing operational evidence before it reaches the incident analyzer.

Runs on:

```text
http://localhost:8090
```

Endpoints:

```text
GET  /actuator/health
POST /api/v1/alerts
GET  /api/v1/context/latest
```

Its responsibility is:

```text
Raw Operational Signals
        ↓
Evidence Collection
        ↓
Normalization
        ↓
Timeline Construction
        ↓
IncidentContext
```

---

# Incident Context Model

The normalized incident context contains:

```text
incident
severity
service
alerts
metrics
logs
health
httpErrors
timeline
```

Supporting models include:

```text
AlertEvidence
LogEvidence
HealthEvidence
HttpErrorEvidence
TimelineEvent
IncidentContext
```

This provides a stable service boundary between evidence collection and incident analysis.

---

# Alert Evidence

Alertmanager alerts are normalized into:

```text
AlertEvidence
```

Fields include:

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
startsAt
endsAt
```

Normalization prevents the analyzer from depending directly on Alertmanager's external webhook representation.

---

# Metric Evidence

The Incident Context Builder currently collects Prometheus evidence including:

```text
http_5xx_rate
request_rate
```

The collected Prometheus responses remain structured evidence.

The system does not fabricate missing metric values when Prometheus returns no result.

---

# Health Evidence

The Incident Context Builder can query:

```text
user-service
order-service
```

health endpoints.

Health information is normalized into:

```text
HealthEvidence
```

Unsupported services safely produce:

```text
UNKNOWN
```

rather than failing the entire incident pipeline.

---

# HTTP Error Evidence

Prometheus HTTP metrics are used to identify HTTP 4xx and 5xx activity associated with the affected service.

Evidence is normalized into:

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

---

# Structured Log Evidence

The platform defines a structured log format and includes a parser for JSON application logs.

The parser produces:

```text
LogEvidence
```

and safely handles malformed records.

A centralized log-ingestion backend has not yet been implemented, so the production log client currently returns an empty collection.

This is intentional.

The Incident Context Builder does not read Docker logs directly and does not require access to the Docker socket.

---

# Deterministic Timeline

Incident timelines are built from observed evidence.

Supported event types include:

```text
ALERT_FIRING
ALERT_RESOLVED
HTTP_ERROR
LOG_EVENT
```

Events are ordered chronologically.

Invalid timestamps are ignored.

The Alertmanager placeholder timestamp:

```text
0001-01-01T00:00:00Z
```

is treated as an absent end timestamp rather than a real resolution event.

---

# Incident Pipeline Resilience

Evidence collection is deliberately best-effort.

A failure in one evidence source should not cause the entire incident-context request to fail.

The service handles:

* Empty alert lists
* Missing alert labels
* Missing annotations
* Unknown services
* Prometheus unavailability
* Malformed structured logs
* Missing timestamps
* Invalid Alertmanager end timestamps

For example, if Prometheus becomes unavailable, metric collections can degrade to empty results while alert and other available evidence remain usable.

---

# AI Incident Analyzer

The AI Incident Analyzer is the final analytical component of the current platform.

Runs on:

```text
http://localhost:8091
```

Endpoints:

```text
GET  /actuator/health
POST /api/v1/analyze
```

The analyzer consumes normalized incident context rather than directly querying monitoring infrastructure.

```text
Incident Context Builder
          │
          │ IncidentContext
          ▼
AI Incident Analyzer
          │
          ▼
Structured IncidentAnalysis
```

---

# AI Analyzer Design

The analyzer is deliberately implemented in layers:

```text
IncidentAnalyzer
       │
       ▼
RuleBasedIncidentAnalyzer
       │
       ├── Existing Stage 12 reasoning
       │
       └── Stage 13 correlation integration
              │
              ▼
       CorrelatedIncident
              │
              ▼
       IncidentAnalysis
```

The `IncidentAnalyzer` interface provides an abstraction boundary for future implementations such as an LLM-backed analyzer.

---

# Stage 13 — Multi-Signal Incident Correlation

Stage 13 introduces explicit evidence correlation inside the AI Incident Analyzer.

The correlation subsystem contains:

```text
Alert Grouping
Temporal Correlation
Service Dependency Correlation
Metric Correlation
Log Correlation
Unified Multi-Signal Correlation
Correlated Incident Mapping
Stage 12 Integration
```

---

## Alert Grouping

Related alerts are grouped using normalized:

```text
service
resource
alert name
```

Alert identity is normalized to avoid duplicate evidence.

The grouping model is:

```text
AlertGroup
```

---

## Temporal Correlation

Temporal correlation identifies alerts occurring within a configurable time window.

Example:

```text
15:40:00  OrderServiceHighLatency
15:40:30  OrderService5xx
```

With a five-minute correlation window, these alerts are temporally related.

The correlation result contains:

```text
alerts
timeWindow
parentAlert
symptomAlerts
correlationScore
```

The score represents temporal proximity.

It does not represent a causal probability.

---

## Service Dependency Correlation

The service dependency layer represents known architectural relationships:

```text
upstream service
        ↓
downstream service
```

For example:

```text
PostgreSQL
    ↓
Order Service
    ↓
API Gateway
```

The dependency correlator identifies alert pairs that correspond to known upstream/downstream relationships.

The dependency graph is deliberately modeled independently from alert data so that architectural relationships can be maintained explicitly.

---

## Metric Correlation

Metric correlation associates structured metric evidence with alerts using:

```text
service
resource
threshold
metric value
timestamp
```

Only threshold-breached metrics are considered relevant.

Correlation strength increases with the amount of supporting metric evidence.

The current correlation model distinguishes:

```text
0 supporting metrics
1 supporting metric
2 supporting metrics
3+ supporting metrics
```

---

## Log Correlation

Log correlation associates structured error/warning log evidence with alerts based on:

```text
service
resource
log level
timestamp
```

The correlator considers:

```text
ERROR
WARN
WARNING
```

signals.

As with metrics, correlation indicates supporting evidence rather than confirmed causation.

---

# Unified Correlation Engine

The:

```text
MultiSignalCorrelationEngine
```

combines:

```text
TemporalCorrelation
ServiceDependencyCorrelation
MetricCorrelation
LogCorrelation
```

into:

```text
MultiSignalCorrelation
```

The engine processes each signal independently before combining the results.

This keeps the correlation model modular and allows additional signal types to be introduced later without replacing the existing architecture.

---

# Correlated Incident

The unified correlation result is mapped into:

```text
CorrelatedIncident
```

containing:

```text
incident
primaryService
likelyRootCause
alerts
correlationTypes
affectedServices
explanation
```

The Stage 13 mapper intentionally does **not** invent a root cause from correlation alone.

Therefore:

```text
likelyRootCause = null
```

when the available evidence does not establish causation.

This is an intentional safety property of the design.

---

# Stage 12 + Stage 13 Integration

The existing deterministic Stage 12 analyzer remains responsible for producing the final:

```text
IncidentAnalysis
```

Stage 13 augments this analysis with correlation evidence.

The resulting pipeline is:

```text
IncidentContext
      │
      ▼
Stage 13 Correlation
      │
      ▼
CorrelatedIncident
      │
      ▼
Rule-Based Incident Analyzer
      │
      ├── Existing evidence reasoning
      ├── Root-cause reasoning
      ├── Confidence scoring
      └── Correlation evidence
      │
      ▼
IncidentAnalysis
```

This approach preserves the existing Stage 12 response contract while adding the richer Stage 13 evidence model.

---

# Correlation Types

The analyzer recognizes:

```text
TEMPORAL
SERVICE_DEPENDENCY
METRIC
LOG
ALERT
MULTI_SIGNAL
```

`MULTI_SIGNAL` is added when more than one independent correlation signal supports the same incident.

---

# Example Stage 13 Flow

A controlled integration test injected two alerts:

```text
OrderServiceHighLatency
OrderService5xx
```

with a 30-second difference.

The resulting analysis contained:

```text
Stage 13 correlation identified
2 correlated alert(s)
using [TEMPORAL] evidence.
```

The analyzer correctly identified:

```text
Incident: OrderServiceHighLatency
Service: order-service
Severity: CRITICAL
```

while avoiding an unsupported root-cause claim.

This demonstrates the intended distinction between:

```text
Correlation
```

and:

```text
Causation
```

---

# Evidence-Backed Reasoning

The analyzer is designed to avoid plausible but unsupported diagnoses.

For example:

```text
Database-related metric
```

does not automatically become:

```text
Database failure
```

unless the incident context contains evidence that supports that conclusion.

Likewise:

```text
Temporal correlation
```

does not automatically become:

```text
Root cause
```

The system therefore favors explicit evidence over speculative diagnosis.

---

# Confidence Scoring

The current confidence scorer is deterministic.

Representative scoring levels include:

```text
Server errors + degraded health → 0.75
Server errors OR degraded health → 0.65
Metrics or logs only → 0.40
No meaningful evidence → 0.10
```

These values are not statistical probabilities.

They represent the deterministic analyzer's confidence in the strength of the available evidence.

---

# Incident Analysis Contract

The analyzer returns:

```text
IncidentAnalysis
```

with:

```text
incident
severity
service
summary
correlation
evidence
probableRootCause
recommendedRemediation
confidence
```

Supporting models include:

```text
AnalysisSeverity
Evidence
Correlation
IncidentAnalysis
```

The response is structured and machine-readable.

---

# No Autonomous Remediation

The AI Incident Analyzer is intentionally read-only.

It does not:

```text
restart containers
modify infrastructure
modify application configuration
execute shell commands
modify databases
scale services
```

Its responsibilities are:

```text
Evidence interpretation
Correlation
Incident analysis
Root-cause assessment
Recommendation
Confidence scoring
```

Deterministic automation remains responsible for any future operational changes.

---

# Future LLM Integration

The analyzer uses:

```text
IncidentAnalyzer
```

as an abstraction.

The current implementation is:

```text
RuleBasedIncidentAnalyzer
```

A future implementation can provide:

```text
LlmIncidentAnalyzer
```

without changing the controller or incident-context contract.

The normalized `IncidentContext` therefore provides a stable foundation for future LLM-assisted incident analysis.

No external LLM integration is currently claimed as implemented.

---

# Testing

The project uses automated tests across the individual services.

## AI Incident Analyzer

Current final suite:

```text
69 tests
69 passed
0 failures
0 errors
```

Coverage includes:

* Incident analysis domain models
* JSON serialization
* Incident Context Client
* HTTP 200 context retrieval
* HTTP 204 no-context handling
* Server-side failure reasoning
* Client-side failure reasoning
* Degraded health reasoning
* Insufficient evidence handling
* Unknown-service handling
* Null and malformed evidence
* Correlation precedence
* Confidence scoring
* Alert grouping
* Temporal correlation
* Service dependency correlation
* Metric correlation
* Log correlation
* Multi-signal correlation
* Correlated incident mapping
* Stage 12 / Stage 13 integration
* Controller analysis flow

Run:

```bash
cd ai-incident-analyzer
mvn test
```

---

## Incident Context Builder

```text
22 tests
22 passed
0 failures
0 errors
```

Coverage includes:

* Alertmanager webhook ingestion
* Alert normalization
* Prometheus evidence
* Health evidence
* HTTP error evidence
* Structured log parsing
* Timeline generation
* Resilience scenarios
* Application context startup

Run:

```bash
cd incident-context
./mvnw test
```

---

## Order Service

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

Coverage includes:

* CRUD operations
* Validation
* Controller behavior
* Service behavior
* Repository interaction
* Failure metrics
* Application context

---

## User Service

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

---

## Current Verified Test Inventory

```text
AI Incident Analyzer       69
Incident Context Builder   22
Order Service              16
User Service                8
API Gateway                 1
────────────────────────────
Total                     116
```

These counts represent the current automated test suites at the completion of Stage 13.

---

# End-to-End Verification

The Docker Compose environment has been used to verify the major runtime paths.

## Application Path

```text
Client
  ↓
API Gateway
  ↓
User Service / Order Service
  ↓
PostgreSQL
```

## Monitoring Path

```text
Application
  ↓
Micrometer
  ↓
Prometheus
  ↓
Grafana
```

## Alerting Path

```text
Prometheus
  ↓
Alertmanager
  ↓
Incident Context Builder
```

## Incident Analysis Path

```text
Incident Context Builder
  ↓
IncidentContext
  ↓
AI Incident Analyzer
  ↓
Stage 13 Correlation
  ↓
IncidentAnalysis
```

---

# Failure Engineering

The platform supports controlled failure testing.

A representative failure workflow is:

```text
Failure Injection
       ↓
Application Impact
       ↓
Metric Change
       ↓
Prometheus Detection
       ↓
Alertmanager
       ↓
Incident Context
       ↓
Evidence Correlation
       ↓
Incident Analysis
       ↓
Remediation Recommendation
       ↓
Manual / Future Deterministic Automation
       ↓
Recovery
       ↓
Alert Resolution
```

Previously verified failure scenarios include:

* Gateway failure
* Database failure
* Service recovery
* Alert firing
* Alert resolution
* Prometheus evidence collection
* Alertmanager delivery
* Incident context generation

The Incident Context Builder has also been tested against:

* Empty alerts
* Missing labels
* Missing annotations
* Unknown services
* Prometheus unavailability
* Malformed logs
* Missing timestamps
* Invalid Alertmanager timestamps

---

# API Examples

## User Service

```bash
curl http://localhost:8080/actuator/health
```

```bash
curl http://localhost:8080/actuator/prometheus
```

Create a user:

```bash
curl -i -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com"
  }'
```

---

## Order Service

```bash
curl http://localhost:8081/actuator/health
```

```bash
curl http://localhost:8081/actuator/prometheus
```

Create an order:

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

---

## API Gateway

```bash
curl -i http://localhost:8082/actuator/health
```

Create a user through the Gateway:

```bash
curl -i -X POST http://localhost:8082/api/users \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: user-request-001" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com"
  }'
```

Create an order through the Gateway:

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

---

## Incident Context Builder

Health:

```bash
curl -s http://localhost:8090/actuator/health
```

Latest context:

```bash
curl -s http://localhost:8090/api/v1/context/latest
```

Alert ingestion:

```bash
curl -sS -X POST http://localhost:8090/api/v1/alerts \
  -H 'Content-Type: application/json' \
  -d '{
    "receiver": "incident-context",
    "status": "firing",
    "groupKey": "example",
    "truncatedAlerts": "0",
    "alerts": [
      {
        "status": "firing",
        "labels": {
          "alertname": "ExampleAlert",
          "severity": "warning",
          "service": "order-service",
          "environment": "local"
        },
        "annotations": {
          "summary": "Example incident"
        },
        "startsAt": "2026-09-25T00:00:00Z",
        "endsAt": "0001-01-01T00:00:00Z",
        "generatorURL": "http://prometheus:9090",
        "fingerprint": "example-alert"
      }
    ]
  }'
```

---

## AI Incident Analyzer

Health:

```bash
curl -s http://localhost:8091/actuator/health
```

Analyze the latest incident:

```bash
curl -sS -X POST http://localhost:8091/api/v1/analyze
```

The analyzer retrieves the latest normalized incident context from the Incident Context Builder.

---

# Prometheus Verification

Prometheus:

```text
http://localhost:9090
```

Check target availability:

```promql
up
```

PostgreSQL availability:

```promql
pg_up
```

Request rate:

```promql
sum by (job) (
  rate(http_server_requests_seconds_count[5m])
)
```

P95 latency:

```promql
histogram_quantile(
  0.95,
  sum by (job, le) (
    rate(http_server_requests_seconds_bucket[5m])
  )
)
```

HTTP 5xx rate:

```promql
sum by (job) (
  rate(http_server_requests_seconds_count{status=~"5.."}[5m])
)
```

PostgreSQL active connections:

```promql
sum by (datname) (
  pg_stat_database_numbackends
)
```

---

# Alertmanager Verification

Alertmanager:

```text
http://localhost:9093
```

Status:

```bash
curl -s http://localhost:9093/api/v2/status
```

Active alerts:

```bash
curl -s http://localhost:9093/api/v2/alerts
```

Prometheus Alertmanager connection:

```bash
curl -s http://localhost:9090/api/v1/alertmanagers
```

---

# Local Development

## Recommended: Docker Compose

From the repository root:

```bash
docker compose up --build
```

Or:

```bash
docker compose up -d --build
```

Verify:

```bash
docker compose ps
```

---

## Individual Services

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

Run Gateway:

```bash
cd gateway
./mvnw spring-boot:run
```

Run Incident Context Builder:

```bash
cd incident-context
./mvnw spring-boot:run
```

Run AI Incident Analyzer:

```bash
cd ai-incident-analyzer
mvn spring-boot:run
```

---

# Service Endpoints

| Component                | Port | Endpoint                        |
| ------------------------ | ---: | ------------------------------- |
| User Service             | 8080 | `http://localhost:8080`         |
| Order Service            | 8081 | `http://localhost:8081`         |
| API Gateway              | 8082 | `http://localhost:8082`         |
| Incident Context Builder | 8090 | `http://localhost:8090`         |
| AI Incident Analyzer     | 8091 | `http://localhost:8091`         |
| Prometheus               | 9090 | `http://localhost:9090`         |
| Alertmanager             | 9093 | `http://localhost:9093`         |
| Grafana                  | 3000 | `http://localhost:3000`         |
| Node Exporter            | 9100 | `http://localhost:9100/metrics` |
| PostgreSQL Exporter      | 9187 | `http://localhost:9187/metrics` |
| PostgreSQL               | 5432 | `localhost:5432`                |

---

# Project Status

The project has progressed through the following implementation stages.

## Stage 1 — Project Initialization ✅

* Spring Boot project foundation
* Project structure
* PostgreSQL environment
* Docker Compose foundation
* Environment configuration

## Stage 2 — User Service ✅

* User CRUD APIs
* PostgreSQL persistence
* JPA / Hibernate
* Flyway
* DTO validation
* Duplicate email handling
* Exception handling
* Actuator
* Automated testing

## Stage 3 — Order Service ✅

* Order CRUD APIs
* Order lifecycle
* Dedicated database schema
* Flyway
* Hibernate validation
* Exception handling
* Application logging
* Custom metrics
* Automated testing

## Stage 4 — API Gateway ✅

* Centralized routing
* User Service routing
* Order Service routing
* Spring `RestClient`
* Correlation ID generation
* Correlation ID propagation
* Request logging
* Downstream error propagation
* Gateway metrics

## Stage 5 — Docker Platform ✅

* Dockerfiles
* Multi-stage builds
* Non-root containers
* Docker networking
* Health checks
* Dependency conditions
* Persistent PostgreSQL storage
* Complete Compose environment

## Stage 6 — Application Observability ✅

* Spring Boot Actuator
* Micrometer
* Prometheus registry
* HTTP metrics
* JVM metrics
* Process metrics
* Database connection metrics
* Custom application metrics
* Runtime metric verification

## Stage 7 — Prometheus Monitoring & Alert Rules ✅

* Prometheus
* Persistent metric storage
* Application scraping
* Node Exporter
* PostgreSQL Exporter
* Application alerts
* Infrastructure alerts
* Database alerts
* `promtool` validation
* 9 alert rules

## Stage 8 — Grafana Dashboards ✅

* Grafana
* Prometheus data source
* Dashboard provisioning
* 5 dashboards
* 41 monitoring panels
* Application monitoring
* JVM monitoring
* Database monitoring
* Service health monitoring

## Stage 9 — Alertmanager & Incident Alerting ✅

* Alertmanager
* Alert routing
* Alert grouping
* Alert lifecycle management
* Alert metadata
* Prometheus integration
* Service failure simulation
* Alert firing and resolution verification
* `amtool` validation

## Stage 10 — Structured Application Logging ✅

* Structured JSON logging
* Correlation IDs
* Request logging
* HTTP status logging
* Request duration logging
* Exception-aware logging
* Machine-readable log format

## Stage 11 — Incident Data Pipeline ✅

* Incident Context Builder
* Alertmanager webhook ingestion
* Alert normalization
* Prometheus evidence
* Health evidence
* HTTP error evidence
* Structured log parser
* Deterministic timeline
* Best-effort evidence collection
* Failure resilience
* Docker E2E verification
* 22/22 automated tests passing

## Stage 12 — AI Incident Analyzer ✅

* Dedicated AI Incident Analyzer
* Incident analysis domain model
* Incident Context client
* Rule-based incident reasoning
* Correlation Engine
* Confidence Scorer
* Evidence-backed analysis
* Root-cause reasoning
* Remediation recommendations
* Read-only decision-support architecture
* Future LLM abstraction
* Docker E2E verification

## Stage 13 — Multi-Signal Incident Correlation ✅

* Alert grouping
* Alert identity normalization
* Temporal correlation
* Service dependency correlation
* Metric correlation
* Log correlation
* Unified multi-signal correlation engine
* Correlation scoring
* `CorrelatedIncident`
* Correlation type classification
* Multi-signal detection
* Incident Context → correlation adapter
* Stage 12 analyzer integration
* Correlation evidence in final incident analysis
* Docker Compose integration
* Controlled two-alert temporal correlation test
* 69/69 AI Incident Analyzer tests passing

---

# Current Platform Capabilities

The completed platform currently provides:

```text
Microservices
     +
API Gateway
     +
PostgreSQL
     +
Docker
     +
Application Metrics
     +
Prometheus
     +
Grafana
     +
Alertmanager
     +
Structured Logging
     +
Incident Context
     +
Evidence Correlation
     +
Deterministic Incident Analysis
```

The incident intelligence layer can reason over:

```text
Alerts
Metrics
Health
HTTP Errors
Logs
Timelines
Temporal Relationships
Service Dependencies
```

The system deliberately separates:

```text
Evidence Collection
        ↓
Evidence Correlation
        ↓
Incident Analysis
        ↓
Operational Recommendation
```

This separation makes the architecture easier to test, extend, and eventually integrate with an LLM without allowing an AI component unrestricted access to infrastructure.

---

# Roadmap

The deterministic incident-analysis foundation is now complete.

Future work may include:

1. Centralized log collection
2. Log aggregation and search
3. Richer structured metric evidence flowing from Incident Context
4. Automated population of service dependency relationships
5. Advanced anomaly detection
6. LLM-assisted incident analysis
7. AI-assisted log analysis
8. Automated incident reports
9. Incident history and persistence
10. Failure simulation workflows
11. Recovery workflows
12. Deterministic automated remediation
13. Security hardening
14. CI/CD
15. Production deployment
16. Portfolio documentation and demonstrations

The intended long-term architecture is:

```text
Application
     ↓
Observability
     ↓
Incident Detection
     ↓
Evidence Collection
     ↓
Evidence Correlation
     ↓
AI-Assisted Analysis
     ↓
Human Review
     ↓
Deterministic Automation
     ↓
Recovery
     ↓
Incident Report
```

---

# Portfolio Value

This project demonstrates practical engineering across:

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
Incident Correlation
Evidence-Based Analysis
Confidence Scoring
AIOps Foundations
```

The project is intentionally broader than a conventional CRUD microservice application.

It demonstrates how an engineering system can progress from:

```text
Business Services
      ↓
Operational Metrics
      ↓
Monitoring
      ↓
Alerting
      ↓
Incident Context
      ↓
Evidence Correlation
      ↓
Incident Analysis
```

while maintaining clear boundaries between application logic, infrastructure, observability, evidence collection, and AI-assisted reasoning.

---

# License

This project is licensed under the [MIT License](LICENSE).

