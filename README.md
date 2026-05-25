Spring Boot Project

A Spring Boot application designed to provide a robust starting point for building Java-based backend services. This project follows standard Spring Boot architecture and encourages modular, clean, and maintainable code.


 Features

- RESTful API built with spring Boot
- Dependency Injection with Spring Framework  
- Spring Data JPA for database persistence  
- Global Exception Handling
- DTOs & Mappers (optional)  
- Environment-based configuration (application.properties / application.yml)  
- Unit & Integration Tests with JUnit & Mockito  
- Spring Security, Swagger/OpenAPI, Docker Integration

---
 Tech Stack

| Technology        | Purpose |
|------------------|---------|
| Java 17+         | Programming language |
| Spring Boot      | Backend framework |
| Maven/Gradle     | Build tool |
| Spring Data JPA  | ORM / Database |
| H2/PostgreSQL/MySQL | Database (configurable) |
| Lombok (optional) | Reduce boilerplate |
| Swagger/OpenAPI (optional) | API documentation |

---
Project Structure
src/
 └── main/
     ├── java/
     │   └── com.example.project/
     │       ├── config/
     │       ├── controller/
     │       ├── service/
     │       ├── service/impl/
     │       ├── repository/
     │       ├── model/
     │       ├── dto/
     │       ├── mapper/
     │       ├── exception/
     │       ├── security/
     │       └── ProjectApplication
     │
     └── resources/
         ├── application.yml
         ├── application-dev.yml
         ├── application-prod.yml
         ├── static/
         └── templates/

 └── test/
     └── java/
         └── com.example.project/
             ├── controller/
             ├── service/
             └── repository/


## Observability


## 🎥 Demo Video

[![Watch Demo](docs/images/image.png)](https://drive.google.com/file/d/1tLSsnLHn7KoqGHjXV2kga15OTQnIPhpz/view?usp=sharing)

This application implements the **three pillars of observability** — Traces, Metrics, and Logs — using industry-standard tools and the **LGTM stack** (Loki, Grafana, Tempo, Micrometer/Prometheus).

---

### Architecture
```Spring Boot Application
│
├── Traces  ──► OTel Collector ──► Grafana Tempo ──► Grafana
├── Metrics ──► Prometheus ──────────────────────► Grafana
└── Logs    ──► Loki ───────────────────────────► Grafana
```

---

### Stack
```
| Component ----------> Purpose 
| OpenTelemetry Collector --------------> Vendor-agnostic trace pipeline 
| Grafana Tempo ----->  Distributed trace storage 
| Prometheus ------------>  Metrics scraping and storage 
| Grafana Loki ----------> Log aggregation 
| Grafana -------------->  Unified visualization dashboard 
| Micrometer ------------>   Metrics and tracing facade 
```
---

### Distributed Tracing

Tracing is implemented using **OpenTelemetry** with **Micrometer Tracing bridge**. Traces are exported via **OTLP protocol** to the OTel Collector, which forwards them to **Grafana Tempo**.

**Auto-instrumentation** captures:
- Incoming HTTP requests
- Database queries via JPA/Hibernate
- Application/JVM metrics


**Manual instrumentation** is implemented via a custom `@Traced` annotation backed by Aspect Oriented Programming(AOP):

```java
@Traced("artifact.findById")
public Artifact findById(String id) {
    // method body
}
```

This creates a child span for the annotated method, visible in Grafana Tempo as a nested span within the parent HTTP request span.

---

### Logging

Logs are shipped to **Grafana Loki** using the `loki-logback-appender`. Every log line is enriched with `traceId` and `spanId` via Micrometer's MDC integration, enabling precise **log-trace correlation**.

**Automatic method logging** is implemented via a custom `@Logged` annotation backed by AOP:

```java
@Logged
public Artifact findById(String id) {
    // method body
}
```

This automatically logs:
- Method entry with arguments
- Method exit with return value and execution time
- Exceptions with error details

**Log-trace correlation** is configured in Grafana — clicking any trace in Tempo jumps directly to the matching logs in Loki for that specific request.

---

### Metrics

Metrics are exposed via **Spring Boot Actuator** at `/actuator/prometheus` and scraped by **Prometheus** every 30 seconds.

**Infrastructure metrics** (auto-configured by Micrometer):
- JVM memory usage (heap and non-heap)
- CPU usage
- HTTP request rate and response times
- Active database connections (HikariCP)
- HTTP error rates (4xx and 5xx)

**Custom business metrics:**

| Metric | Type | Tags | Description |
|--------|------|------|-------------|
| `artifacts_operations_total` | Counter | `operation` (searched, created, deleted) | Tracks artifact operations |
| `auth_attempts_total` | Counter | `result` (success, login_failure, token_failure) | Tracks authentication attempts |

---

### Grafana Dashboard

A unified Grafana dashboard provides real-time visibility into the application:

- JVM Memory Usage
- HTTP Request Rate
- HTTP Error Rate
- CPU Usage
- Average Response Time
- Active Database Connections
- Artifact Operations
- Authentication Attempts

---

### Running Locally

Start the full observability stack using Docker Compose:

```bash
docker compose up -d
```
```
| Service  ------->  URL 
| Grafana -------> http://localhost:3000 
| Prometheus ------> http://localhost:9090 
| Grafana Tempo -----> http://localhost:3200 
| Grafana Loki ---->  http://localhost:3100 
```


---

### Configuration

Observability is configured via Spring Boot profiles:

| Property | Dev | Prod |
|----------|-----|------|
| OTLP endpoint | `http://localhost:4318/v1/traces` | Injected via environment variable |
| Sampling probability | `1.0` (100%) | `0.1` (10%) |
| Environment tag | `dev` | `prod` |

---


