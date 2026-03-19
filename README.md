# Microservices Pet Project

A multi-module Java application built around a microservices architecture. The project covers both foundational programming tasks and a full-stack distributed system with authentication, order management, payment processing, and an API gateway — all containerized and connected through a CI/CD pipeline.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Services](#services)
- [Tech Stack](#tech-stack)
- [Foundational Modules](#foundational-modules)
- [Prerequisites](#prerequisites)
- [Running with Docker Compose](#running-with-docker-compose)
- [Running Locally](#running-locally)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [CI/CD Pipeline](#cicd-pipeline)
- [Project Structure](#project-structure)

---

## Overview

The repository is structured as a Maven multi-module project. The first four modules are self-contained algorithmic exercises. From task_5 onward, everything converges into a single distributed platform for order and payment processing.

Core concepts covered:

- Microservice architecture with clear separation of concerns
- JWT authentication with access and refresh token rotation
- Asynchronous inter-service communication via Apache Kafka
- Response caching with Redis
- Database schema management with Liquibase
- Full containerization with Docker and Docker Compose
- Automated CI/CD with GitHub Actions and SonarCloud analysis

---

## Architecture

```
                    +----------------------+
                    |     API Gateway      |
                    |        :8083         |
                    |   (Spring WebFlux)   |
                    +----------+-----------+
                               | JWT filter
         +---------------------+---------------------+
         |                     |                     |
  +------+------+      +-------+------+      +-------+------+
  | Auth Service|      | User Service |      |Order Service |
  |    :8082    |      |    :8081     |      |    :8080     |
  | (PostgreSQL)|      | (PostgreSQL  |      | (PostgreSQL) |
  |             |      |  + Redis)    |      |              |
  +-------------+      +--------------+      +------+-------+
                                                    | Kafka
                                             +------+-------+
                                             |Payment Service|
                                             |    :8085      |
                                             | (PostgreSQL)  |
                                             +--------------+
```

**Registration flow (orchestrated by API Gateway):**

1. Client sends `POST /auth/register` to the gateway
2. Gateway creates the user profile in User Service
3. Gateway creates credentials in Auth Service
4. If either step fails, the gateway automatically rolls back by deleting the already-created user

**Order and payment flow (event-driven via Kafka):**

1. Order Service creates an order and publishes an event to `create-order-topic`
2. Payment Service consumes the event, processes the payment, and publishes the result
3. Order Service receives the result and updates the order status accordingly

---

## Services

### Auth Service — port 8082

Handles authentication and token lifecycle.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register new credentials |
| POST | `/auth/login` | Authenticate and receive JWT tokens |
| POST | `/auth/refresh` | Get a new access token using a refresh token |
| POST | `/auth/validate` | Validate a token and return user info |
| GET | `/auth/health` | Health check |

Details:
- Access token TTL: 15 minutes
- Refresh token TTL: 24 hours, persisted in PostgreSQL
- Passwords hashed with BCrypt
- Roles: `USER`, `ADMIN`

---

### User Service — port 8081

Manages user profiles and associated payment cards.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/users` | Create a user |
| GET | `/users/{id}` | Get user by ID |
| GET | `/users` | List all users with pagination |
| GET | `/users/email/{email}` | Find user by email |
| PUT | `/users/{id}` | Update user |
| DELETE | `/users/{id}` | Delete user |
| POST | `/cards` | Add a card to a user |
| GET | `/cards/{id}` | Get card by ID |
| DELETE | `/cards/{id}` | Remove card |

Details:
- User data cached in Redis with a 10-minute TTL
- JWT filter protects all endpoints
- Card numbers enforced as unique

---

### Order Service — port 8080

Manages orders and communicates with Payment Service through Kafka.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/orders` | Create an order |
| GET | `/api/v1/orders/{id}` | Get order by ID |
| GET | `/api/v1/orders/by-ids` | Get multiple orders by ID list |
| GET | `/api/v1/orders/by-status` | Filter orders by status |
| PUT | `/api/v1/orders/{id}` | Update order |
| PATCH | `/api/v1/orders/{id}/status` | Update order status |
| DELETE | `/api/v1/orders/{id}` | Delete order |

Order statuses: `PENDING` → `PROCESSING` → `PAID` / `FAILED`

---

### Payment Service — port 8085

Processes payments triggered by Kafka events or direct API calls.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payments` | Create a payment manually |
| GET | `/api/payments/order/{orderId}` | Payments by order |
| GET | `/api/payments/user/{userId}` | Payments by user |
| GET | `/api/payments/search` | Filter by status |
| GET | `/api/payments/summary` | Total amount for a time range |

Kafka topics:
- Consumes: `create-order-topic`
- Publishes: `payment-result-topic`

---

### API Gateway — port 8083

Single entry point built on Spring WebFlux.

- JWT validation at the gateway level
- Orchestrates registration with rollback support
- Token caching in Redis
- CORS configuration

---

## Tech Stack

| Category | Technologies |
|----------|-------------|
| Language / Runtime | Java 21, Maven (multi-module) |
| Frameworks | Spring Boot 3.x, Spring Security, Spring WebFlux |
| Persistence | Spring Data JPA, PostgreSQL 15, Liquibase |
| Messaging / Cache | Apache Kafka (Confluent 7.4), Zookeeper, Redis 7 |
| Security | JWT (jjwt 0.11.5), BCrypt |
| API Docs | SpringDoc OpenAPI, Swagger UI |
| Mapping | MapStruct 1.5.5 |
| Utilities | Lombok |
| Testing | JUnit 5, Mockito, Testcontainers, Embedded Redis |
| Code Quality | JaCoCo, SonarCloud |
| Containerization | Docker, Docker Compose |
| CI/CD | GitHub Actions, GitHub Container Registry |

---

## Foundational Modules

### task_1 — Custom LinkedList

A generic doubly linked list `MyLinkedList<E>` written from scratch without any Java collections. Implements `addFirst`, `addLast`, `removeFirst`, `removeLast`, index-based access, and iteration.

### task_2 — Order Analytics with Streams API

An analytics engine for e-commerce order data using the Java Stream API. Metrics include average order value, most popular products, revenue by city, total revenue, and returning customer identification.

### task_3 — Multithreaded Factory Simulation

A simulation of a robot assembly factory where multiple factions compete for shared resources. Uses `ExecutorService`, demonstrates thread synchronization, and shows safe access to shared mutable state.

### task_4 — MiniSpring IoC Container

A lightweight dependency injection container that replicates core Spring behavior using reflection:
- Custom annotations: `@Component`, `@Autowired`, `@Configuration`, `@Bean`, `@Scope`
- Singleton and Prototype scopes
- Bean lifecycle via `InitializingBean`
- Classpath scanning and field injection through reflection

---

## Prerequisites

- Java 21 or later
- Maven 3.9 or later
- Docker and Docker Compose
- Git

```bash
java -version
mvn -version
docker -version
```

---

## Running with Docker Compose

The simplest way to start the full platform:

```bash
git clone https://github.com/graffegiraffe/pet_project_kate_rublevskaya.git
cd pet_project_kate_rublevskaya

git checkout dev

docker compose -f docker-compose-full.yml up --build
```

To run in the background:

```bash
docker compose -f docker-compose-full.yml up --build -d
```

Once all containers are healthy, the following endpoints are available:

| Service | URL |
|---------|-----|
| API Gateway | http://localhost:8083 |
| Auth Service + Swagger | http://localhost:8082/swagger-ui.html |
| User Service + Swagger | http://localhost:8081/swagger-ui.html |
| Order Service + Swagger | http://localhost:8080/swagger-ui.html |
| Payment Service + Swagger | http://localhost:8085/swagger-ui.html |

To stop and clean up:

```bash
# Stop all containers
docker compose -f docker-compose-full.yml down

# Stop and remove all volumes (full data reset)
docker compose -f docker-compose-full.yml down -v
```

---

## Running Locally

Each service can be started independently for development or debugging.

**Step 1 — Start infrastructure**

```bash
# PostgreSQL
docker run -d --name pg-local \
  -e POSTGRES_USER=katusha \
  -e POSTGRES_PASSWORD=katusha \
  -e POSTGRES_DB=postgres \
  -p 5432:5432 postgres:15-alpine

# Redis
docker run -d --name redis-local -p 6379:6379 redis:7-alpine

# Kafka and Zookeeper
docker compose -f docker-compose-full.yml up zookeeper kafka -d
```

**Step 2 — Start services**

Each service should be started in a separate terminal:

```bash
cd authService
mvn spring-boot:run -Dspring-boot.run.profiles=local

cd UserService
mvn spring-boot:run -Dspring-boot.run.profiles=local

cd orderservice
mvn spring-boot:run -Dspring-boot.run.profiles=local

cd paymentservice
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Default configuration (`application-local.properties`)**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=katusha
spring.datasource.password=katusha

spring.data.redis.host=localhost
spring.data.redis.port=6379

jwt.secret=231962b41fccd0163cb87032579b0185
jwt.access-token-expiration=900000
jwt.refresh-token-expiration=86400000
```

---

## API Documentation

Swagger UI is available for each service after startup:

```
http://localhost:8082/swagger-ui.html   (Auth Service)
http://localhost:8081/swagger-ui.html   (User Service)
http://localhost:8080/swagger-ui.html   (Order Service)
http://localhost:8085/swagger-ui.html   (Payment Service)
```

Example — register, login, and make an authenticated request:

```bash
# Register
curl -X POST http://localhost:8082/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "user@example.com", "password": "password123", "role": "USER"}'

# Login
curl -X POST http://localhost:8082/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user@example.com", "password": "password123"}'

# Use the token
curl -X GET http://localhost:8081/users/1 \
  -H "Authorization: Bearer <access_token>"
```

---

## Testing

The project includes unit tests and integration tests with Testcontainers.

```bash
# Run all tests for UserService
mvn test -pl UserService

# Run tests for a specific module
mvn test -pl paymentservice

# Run tests and generate a JaCoCo coverage report
mvn clean verify -pl UserService
# Report: UserService/target/site/jacoco/index.html
```

Test coverage breakdown for UserService:

| Layer | Approach |
|-------|----------|
| Service | JUnit 5 + Mockito |
| Controller | MockMvc integration tests |
| Repository | Testcontainers + real PostgreSQL instance |
| Cache | Embedded Redis |

---

## CI/CD Pipeline

GitHub Actions runs on every push and pull request targeting `main` or `dev`.

Stages:

1. **Build and Test** — Maven compile and UserService tests on Java 21
2. **SonarCloud Analysis** — static analysis with JaCoCo coverage report (push only)
3. **Docker Build** — builds the UserService image and pushes it to GitHub Container Registry (push only)

Published image: `ghcr.io/graffegiraffe/userservice`

---

## Project Structure

```
pet_project_kate_rublevskaya/
|
+-- .github/workflows/
|   +-- ci-pipeline.yml
|
+-- apigateway/              Spring WebFlux gateway, port 8083
|   +-- src/.../filter/      JWT authentication filter
|   +-- src/.../handler/     Registration orchestration with rollback
|
+-- authService/             Authentication service, port 8082
|   +-- src/.../entity/      UserCredential, RefreshToken, Role
|   +-- src/.../service/     AuthService, JwtService
|
+-- UserService/             User management service, port 8081
|   +-- src/.../entity/      User, CardInfo
|   +-- src/.../config/      RedisConfig, SecurityConfig
|   +-- src/test/            Unit and integration tests
|
+-- orderservice/            Order management service, port 8080
|   +-- src/.../model/       Order, OrderItem, OrderStatus
|   +-- src/.../kafka/       Kafka producer and consumer
|
+-- paymentservice/          Payment processing service, port 8085
|   +-- src/.../model/       Payment, PaymentStatus
|   +-- src/.../kafka/       PaymentConsumer, PaymentProducer
|
+-- task_1/                  Custom LinkedList implementation
+-- task_2/                  Order analytics with Streams API
+-- task_3/                  Multithreaded factory simulation
+-- task_4/                  MiniSpring IoC container
|
+-- docker-compose-full.yml  Full platform startup
+-- pom.xml                  Root multi-module Maven POM
```

---

## Author

Kate Rublevskaya
