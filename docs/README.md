# EventHub

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue)
![Docker](https://img.shields.io/badge/Docker-ready-blue)
![JWT](https://img.shields.io/badge/Security-JWT-orange)
![JaCoCo](https://img.shields.io/badge/JaCoCo-84%25-brightgreen)

**EventHub** is a backend REST API application for event management, ticket sales, user registrations, organizer applications, reviews, payments, statistics and administrative control.

The project was developed with **Spring Boot**, **Spring Security**, **JWT authentication**, **Spring Data JPA / Hibernate**, **PostgreSQL**, **Flyway**, **Swagger/OpenAPI**, **Docker**, **Actuator**, **AOP audit logging**, **JUnit 5**, **Mockito** and **JaCoCo**.

---

## Table of Contents

- [Functional Features](#functional-features)
- [Technology Stack](#technology-stack)
- [System Roles](#system-roles)
- [Core Business Flow](#core-business-flow)
- [Main Business Rules](#main-business-rules)
- [Project Architecture](#project-architecture)
- [Database Design](#database-design)
- [Environment Variables](#environment-variables)
- [Run with Docker Compose](#run-with-docker-compose)
- [Run Locally without Docker](#run-locally-without-docker)
- [API Documentation with Swagger](#api-documentation-with-swagger)
- [Authentication and JWT Usage](#authentication-and-jwt-usage)
- [API Endpoints](#api-endpoints)
- [Actuator Endpoints](#actuator-endpoints)
- [Logging and Audit Logs](#logging-and-audit-logs)
- [Testing and JaCoCo Coverage](#testing-and-jacoco-coverage)
- [JavaDoc](#javadoc)
- [Useful Docker Commands](#useful-docker-commands)
- [HTTP Status Codes](#http-status-codes)
- [Possible Future Improvements](#possible-future-improvements)

---

## Functional Features

The application implements a complete backend workflow for an event platform:

- user registration and login;
- JWT-based authentication and authorization;
- role-based access control for guests, users, organizers and administrators;
- user profile management;
- admin user management with block/unblock actions;
- organizer application workflow;
- event category management;
- event location management;
- event CRUD operations;
- event publishing, cancellation and automatic finishing;
- ticket type management for events;
- ticket purchase and event registration;
- registration cancellation with simulated refund;
- simulated payment records;
- event reviews and automatic average rating recalculation;
- organizer and admin statistics;
- AOP-based audit logging;
- global exception handling;
- request validation;
- pagination, sorting, searching and filtering;
- Swagger/OpenAPI documentation;
- Dockerized application startup;
- Actuator health, info and metrics endpoints;
- unit and controller tests with JaCoCo coverage above 80%.

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Main programming language |
| Spring Boot 3.5 | Application framework |
| Spring Web | REST API layer |
| Spring Security | Authentication and authorization |
| JWT / JJWT | Stateless token-based authentication |
| Spring Data JPA | Database access layer |
| Hibernate | ORM implementation |
| PostgreSQL | Relational database |
| Flyway | Database migrations |
| Maven | Build and dependency management |
| Lombok | Reducing boilerplate code |
| Bean Validation | DTO validation |
| Spring AOP | Audit logging aspect |
| Spring Boot Actuator | Health, info and metrics endpoints |
| Springdoc OpenAPI / Swagger UI | API documentation |
| Docker / Docker Compose | Containerized application startup |
| JUnit 5 | Unit testing |
| Mockito | Mocking in tests |
| MockMvc | Controller layer tests |
| JaCoCo | Test coverage report |

---

## System Roles

The project uses role-based access control.

| Role | Description |
|---|---|
| `GUEST` | Unauthenticated visitor. Can view public data such as events, categories, locations, ticket types and reviews. |
| `ROLE_USER` | Regular authenticated user. Can buy tickets, cancel own registrations, leave reviews and apply to become an organizer. |
| `ROLE_ORGANIZER` | Approved organizer. Can create and manage own events, ticket types, view participants, payments and statistics. |
| `ROLE_ADMIN` | Platform administrator. Can manage users, categories, organizer applications, audit logs, payments and platform statistics. |

A newly registered account receives only `ROLE_USER`.  
The organizer role is not assigned during registration. A user must submit an organizer application, and only an administrator can approve it.

---

## Core Business Flow

### 1. User registration and login

A visitor registers through:

```http
POST /api/auth/register
```

After successful registration, the user receives `ROLE_USER` and can authenticate through:

```http
POST /api/auth/login
```

The login response contains a JWT access token.

---

### 2. Becoming an organizer

A regular user can submit an organizer application:

```http
POST /api/organizer-applications
```

The application is created with the `PENDING` status.

An administrator reviews applications through:

```http
GET /api/admin/organizer-applications
PATCH /api/admin/organizer-applications/{id}/approve
PATCH /api/admin/organizer-applications/{id}/reject
```

If the application is approved, the user receives `ROLE_ORGANIZER` and can create events.

---

### 3. Event creation and publishing

An organizer creates an event in `DRAFT` status:

```http
POST /api/events
```

Then the event can be published:

```http
PATCH /api/events/{id}/publish
```

Only published events are available for ticket purchase.

---

### 4. Ticket purchase and registration

A user buys a ticket by selecting a ticket type:

```http
POST /api/registrations
```

The application checks ticket availability, event status, user status and business restrictions.  
If the purchase is successful:

- ticket available quantity is decreased;
- registration is created with `ACTIVE` status;
- payment is created with `PAID` status.

Ticket purchase uses a database lock to protect the system from selling the last ticket twice.

---

### 5. Registration cancellation

A user can cancel their own active registration before the event starts:

```http
PATCH /api/registrations/{id}/cancel
```

After cancellation:

- registration status becomes `CANCELLED`;
- ticket available quantity is increased;
- payment status becomes `REFUNDED`.

---

### 6. Reviews and rating

A user can leave a review only if they have an active registration for a finished event:

```http
POST /api/events/{eventId}/reviews
```

After creating, updating or deleting a review, the event average rating is recalculated automatically.

---

## Main Business Rules

The project contains the following business rules and validations:

- user email must be unique;
- username must be unique;
- passwords are stored in encrypted form using BCrypt;
- blocked users cannot log in or perform important actions;
- users cannot receive organizer role without admin approval;
- a user cannot create another organizer application while they already have `PENDING` or `APPROVED` application;
- if an organizer application is rejected, the user can submit a new one later;
- only organizers can create events;
- organizers can manage only their own events unless the current user is an admin;
- blocked organizers cannot create events;
- event start date must be in the future;
- event end date must be after start date;
- event capacity must be greater than zero;
- event category and location must exist;
- published events cannot be deleted if business rules do not allow it;
- ticket price cannot be negative;
- ticket total quantity must be greater than zero;
- total ticket quantity for an event cannot exceed event capacity;
- ticket type with active registrations cannot be deleted;
- only published events can be used for ticket purchase;
- tickets cannot be bought for cancelled, finished or already started events;
- available ticket quantity must be greater than zero;
- a user cannot have two active registrations for the same event;
- an organizer cannot buy a ticket for their own event;
- a user can cancel only their own registration;
- a registration cannot be cancelled after the event has started;
- reviews are allowed only for users who participated in the event;
- a user cannot leave more than one review for the same event;
- review rating must be from 1 to 5;
- review comment length is limited;
- admin cannot block their own account;
- important actions are stored in the `audit_logs` table.

---

## Project Architecture

The project follows a classic layered architecture.

```text
src/main/java/com/eventhub
├── aspect              # AOP annotation and audit logging aspect
├── config              # Security, OpenAPI and encoder configuration
├── controller          # REST controllers
├── dto
│   ├── request          # Request DTOs with validation annotations
│   └── response         # Response DTOs returned by API
├── entity              # JPA entities
├── enums               # Application enums and statuses
├── exception           # Global and custom business exceptions
├── mapper              # Entity-to-DTO mappers
├── repository          # Spring Data JPA repositories
├── scheduler           # Scheduled event status update job
├── security            # JWT, custom user details and security handlers
└── service
    └── impl            # Business logic implementation
```

### Layer Responsibilities

| Layer | Responsibility |
|---|---|
| `controller` | Accepts HTTP requests and returns HTTP responses. |
| `dto.request` | Describes input data and validation rules. |
| `dto.response` | Describes response models returned by API. |
| `service` | Contains business logic and transaction boundaries. |
| `repository` | Provides database access through Spring Data JPA. |
| `entity` | Describes database tables and relationships. |
| `mapper` | Converts entities to response DTOs. |
| `exception` | Contains custom exceptions and global error handling. |
| `security` | Handles JWT authentication and role-based access. |
| `aspect` | Logs important business actions into audit logs. |
| `scheduler` | Automatically finishes past published events. |

---

## Database Design

The application uses PostgreSQL and Flyway migrations.

The database contains 12 main tables:

| Table | Purpose |
|---|---|
| `users` | Stores registered users. |
| `roles` | Stores system roles. |
| `user_roles` | Many-to-many relation between users and roles. |
| `organizer_applications` | Stores user requests to become organizers. |
| `event_categories` | Stores event categories. |
| `locations` | Stores event locations. |
| `events` | Stores events created by organizers. |
| `ticket_types` | Stores ticket types for events. |
| `registrations` | Stores ticket purchases and event registrations. |
| `reviews` | Stores user reviews for events. |
| `payments` | Stores simulated payment records. |
| `audit_logs` | Stores important actions logged by AOP. |

### Main Relationships

- `users` many-to-many `roles` through `user_roles`;
- `users` one-to-many `events`;
- `users` one-to-many `registrations`;
- `users` one-to-many `reviews`;
- `event_categories` one-to-many `events`;
- `locations` one-to-many `events`;
- `events` one-to-many `ticket_types`;
- `events` one-to-many `registrations`;
- `events` one-to-many `reviews`;
- `ticket_types` one-to-many `registrations`;
- `registrations` one-to-one `payments`.

### Flyway Migrations

Migration files are stored in:

```text
src/main/resources/db/migration
```

Current migrations:

```text
V1__create_roles_table.sql
V2__create_users_table.sql
V3__create_user_roles_table.sql
V4__create_organizer_applications_table.sql
V5__create_event_categories_table.sql
V6__create_locations_table.sql
V7__create_events_table.sql
V8__create_ticket_types_table.sql
V9__create_registrations_table.sql
V10__create_reviews_table.sql
V11__create_payments_table.sql
V12__create_audit_logs_table.sql
V13__insert_initial_data.sql
```

Additional database documentation is available in:

```text
docs/database-design.md
```

---

## Environment Variables

The application can be configured through environment variables.

| Variable | Description | Default value |
|---|---|---|
| `POSTGRES_DB` | PostgreSQL database name | `eventhub` |
| `POSTGRES_USER` | PostgreSQL username | `eventhub_user` |
| `POSTGRES_PASSWORD` | PostgreSQL password | `eventhub_password` |
| `POSTGRES_PORT` | External PostgreSQL port | `5433` |
| `APP_PORT` | External application port | `8080` |
| `DB_URL` | JDBC database URL | `jdbc:postgresql://postgres:5432/eventhub` in Docker |
| `DB_USERNAME` | Application database username | value of `POSTGRES_USER` |
| `DB_PASSWORD` | Application database password | value of `POSTGRES_PASSWORD` |
| `JWT_SECRET` | Secret key for signing JWT tokens | value from `.env.example` |
| `JWT_EXPIRATION` | JWT lifetime in milliseconds | `86400000` |

Example environment file:

```text
.env.example
```

---

## Run with Docker Compose

This is the recommended way to run the project.

The application image is published on DockerHub:

```text
iamm1st/eventhub:latest
```

The full stack is started with Docker Compose because the application requires PostgreSQL.

Docker Compose starts two containers:

| Container | Image | Purpose |
|---|---|---|
| `eventhub-app` | `iamm1st/eventhub:latest` | Spring Boot application |
| `eventhub-postgres` | `postgres:17-alpine` | PostgreSQL database |

### 1. Clone the repository

```bash
git clone https://github.com/iamm1st/eventhub.git
cd eventhub
```

If the repository URL is different, use the actual GitHub repository URL.

### 2. Create `.env` file

For Windows PowerShell:

```powershell
copy .env.example .env
```

For Linux/macOS:

```bash
cp .env.example .env
```

The project also has default values in `docker-compose.yml`, so it can start even without changing `.env`.

### 3. Pull Docker images

```bash
docker compose pull
```

This command downloads:

```text
iamm1st/eventhub:latest
postgres:17-alpine
```

### 4. Start the application

```bash
docker compose up
```

Or start it in detached mode:

```bash
docker compose up -d
```

After startup, the application is available at:

```text
http://localhost:8080
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

PostgreSQL is available from the host machine at:

```text
localhost:5433
```

Inside Docker network, the application connects to PostgreSQL by service name:

```text
postgres:5432
```

---

## Run Locally without Docker

### Requirements

- JDK 21+
- Maven 3.8+ or included Maven Wrapper
- PostgreSQL 14+

### 1. Create PostgreSQL database

Create a local PostgreSQL database:

```sql
CREATE DATABASE eventhub;
```

By default, local `application.yml` expects:

```text
DB_URL=jdbc:postgresql://localhost:5432/eventhub
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

You can override these values through environment variables.

### 2. Run the application

For Windows:

```bash
.\mvnw.cmd spring-boot:run
```

For Linux/macOS:

```bash
./mvnw spring-boot:run
```

Flyway migrations will be executed automatically on startup.

---

## API Documentation with Swagger

Swagger UI is enabled in the project and available after application startup:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON documentation is available at:

```text
http://localhost:8080/v3/api-docs
```

Swagger documentation allows you to:

- view all API endpoints;
- check request and response models;
- see validation rules;
- see possible response status codes;
- execute API requests directly from the browser;
- authorize requests using JWT.

### Swagger JWT Authorization

1. Login through `POST /api/auth/login`.
2. Copy the `accessToken` value from the response.
3. Click the **Authorize** button in Swagger.
4. Paste the token without the `Bearer` prefix.
5. Execute secured endpoints.

---

## Authentication and JWT Usage

The application uses JWT access tokens.

### Register User

```http
POST /api/auth/register
Content-Type: application/json
```

Example request:

```json
{
  "username": "john",
  "email": "john@mail.com",
  "password": "123456"
}
```

A new user receives `ROLE_USER`.

### Login User

```http
POST /api/auth/login
Content-Type: application/json
```

Example request:

```json
{
  "email": "admin@eventhub.com",
  "password": "admin123"
}
```

Example response:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@eventhub.com",
    "status": "ACTIVE",
    "roles": [
      "ROLE_ADMIN"
    ]
  }
}
```

### Use JWT Token

For protected endpoints, pass the token in the HTTP header:

```http
Authorization: Bearer <JWT_TOKEN>
```

---

## Default Data

Flyway inserts initial data on application startup.

### Default Administrator

| Field | Value |
|---|---|
| Email | `admin@eventhub.com` |
| Password | `admin123` |
| Role | `ROLE_ADMIN` |

### Default Categories

- IT
- Music
- Sport
- Education
- Business
- Art

### Default Locations

- Conference Hall, Minsk
- Event Space, Minsk
- City Cultural Center, Grodno

---

## API Endpoints

Base URL:

```text
http://localhost:8080
```

Most application endpoints use the `/api` prefix.

### Authentication

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a new user with `ROLE_USER`. |
| `POST` | `/api/auth/login` | Public | Authenticate user and return JWT token. |

---

### User Profile

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/users/me` | USER / ORGANIZER / ADMIN | Get current user profile. |
| `PUT` | `/api/users/me` | USER / ORGANIZER / ADMIN | Update current user profile. |

---

### Admin User Management

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/admin/users` | ADMIN | Get paginated list of users. |
| `GET` | `/api/admin/users/{id}` | ADMIN | Get user by id. |
| `PATCH` | `/api/admin/users/{id}/block` | ADMIN | Block user account. |
| `PATCH` | `/api/admin/users/{id}/unblock` | ADMIN | Unblock user account. |

---

### Organizer Applications

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/organizer-applications` | USER | Submit an application to become organizer. |
| `GET` | `/api/organizer-applications/my` | USER | Get current user's organizer applications. |
| `GET` | `/api/admin/organizer-applications` | ADMIN | Get organizer applications, optionally filtered by status. |
| `PATCH` | `/api/admin/organizer-applications/{id}/approve` | ADMIN | Approve organizer application and grant `ROLE_ORGANIZER`. |
| `PATCH` | `/api/admin/organizer-applications/{id}/reject` | ADMIN | Reject organizer application with admin comment. |

---

### Categories

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/categories` | Public | Get paginated categories. |
| `GET` | `/api/categories/{id}` | Public | Get category by id. |
| `POST` | `/api/categories` | ADMIN | Create category. |
| `PUT` | `/api/categories/{id}` | ADMIN | Update category. |
| `DELETE` | `/api/categories/{id}` | ADMIN | Delete category if it is not used by events. |

---

### Locations

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/locations` | Public | Get paginated locations. |
| `GET` | `/api/locations/{id}` | Public | Get location by id. |
| `POST` | `/api/locations` | ORGANIZER / ADMIN | Create location. |
| `PUT` | `/api/locations/{id}` | ORGANIZER / ADMIN | Update location. |
| `DELETE` | `/api/locations/{id}` | ADMIN | Delete location if it is not used by events. |

---

### Events

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/events` | Public | Get paginated events with optional filtering. |
| `GET` | `/api/events/{id}` | Public | Get full event information by id. |
| `POST` | `/api/events` | ORGANIZER | Create event in `DRAFT` status. |
| `PUT` | `/api/events/{id}` | Event owner / ADMIN | Update event. |
| `PATCH` | `/api/events/{id}/publish` | Event owner / ADMIN | Publish event. |
| `PATCH` | `/api/events/{id}/cancel` | Event owner / ADMIN | Cancel event. |
| `DELETE` | `/api/events/{id}` | Event owner / ADMIN | Delete event if business rules allow it. |

Event list supports pagination, sorting, searching and filtering:

```http
GET /api/events?page=0&size=10&sort=startDate,asc
GET /api/events?categoryId=1&city=Minsk&keyword=java
```

---

### Ticket Types

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/events/{eventId}/ticket-types` | Event owner / ADMIN | Create ticket type for event. |
| `GET` | `/api/events/{eventId}/ticket-types` | Public | Get ticket types for event. |
| `GET` | `/api/ticket-types/{id}` | Public | Get ticket type by id. |
| `PUT` | `/api/ticket-types/{id}` | Event owner / ADMIN | Update ticket type. |
| `DELETE` | `/api/ticket-types/{id}` | Event owner / ADMIN | Delete ticket type if it has no active registrations. |

---

### Registrations and Ticket Purchase

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/registrations` | USER | Buy ticket and create active registration. |
| `PATCH` | `/api/registrations/{id}/cancel` | USER | Cancel own active registration. |
| `GET` | `/api/registrations/my` | USER | Get current user's registrations. |
| `GET` | `/api/events/{eventId}/registrations` | ORGANIZER / ADMIN | Get registrations for event. |

Ticket purchase is transactional and uses pessimistic locking to prevent double-selling the last ticket.

---

### Reviews

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/events/{eventId}/reviews` | USER | Create review for finished event. |
| `GET` | `/api/events/{eventId}/reviews` | Public | Get paginated event reviews. |
| `PUT` | `/api/reviews/{id}` | Review owner | Update own review. |
| `DELETE` | `/api/reviews/{id}` | Review owner / ADMIN | Delete review. |

After every review change, the event average rating is recalculated.

---

### Payments

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/admin/payments` | ADMIN | Get paginated list of all payments. |
| `GET` | `/api/admin/payments/{id}` | ADMIN | Get payment by id. |
| `GET` | `/api/organizer/payments` | ORGANIZER | Get payments for current organizer's events. |

Payment processing is simulated. A successful ticket purchase creates a `PAID` payment, and registration cancellation changes it to `REFUNDED`.

---

### Statistics

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/organizer/statistics` | ORGANIZER | Get statistics for current organizer. |
| `GET` | `/api/admin/statistics` | ADMIN | Get platform statistics. |

Organizer statistics include:

- events count;
- sold tickets;
- total revenue;
- average rating;
- most popular event.

Admin statistics include:

- users count;
- organizers count;
- events count;
- registrations count;
- total revenue;
- blocked users count.

---

### Audit Logs

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/admin/audit-logs` | ADMIN | Get paginated audit logs. |

---

## Actuator Endpoints

Spring Boot Actuator is enabled for health, info and metrics.

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/actuator/health` | ADMIN | Application health status. |
| `GET` | `/actuator/info` | ADMIN | Application information. |
| `GET` | `/actuator/metrics` | ADMIN | Application metrics. |

Actuator endpoints are protected by Spring Security and require `ROLE_ADMIN`.

---

## Logging and Audit Logs

The project uses two logging mechanisms.

### Application Logs

Application logs are written to:

```text
logs/eventhub.log
```

Console and file logging are configured in `application.yml`.

### AOP Audit Logging

Important business actions are logged through a custom annotation:

```java
@LogAction(action = "BUY_TICKET", entityType = "REGISTRATION", useReturnedId = true)

public RegistrationResponse buyTicket(RegistrationCreateRequest request) {
    // business logic
}
```

The `LoggingAspect` intercepts annotated service methods and stores audit data in the `audit_logs` table.

Examples of audited actions:

- `CREATE_EVENT`
- `UPDATE_EVENT`
- `PUBLISH_EVENT`
- `CANCEL_EVENT`
- `DELETE_EVENT`
- `BUY_TICKET`
- `CANCEL_REGISTRATION`
- `CREATE_REVIEW`
- `UPDATE_REVIEW`
- `DELETE_REVIEW`
- `BLOCK_USER`
- `UNBLOCK_USER`
- `APPROVE_ORGANIZER_APPLICATION`
- `REJECT_ORGANIZER_APPLICATION`

Audit logs contain:

- username;
- action;
- entity type;
- entity id;
- success flag;
- error message if action failed;
- execution time;
- creation date.

---

## Testing and JaCoCo Coverage

The project contains unit, controller, security, mapper, exception and infrastructure tests.

Test packages:

```text
src/test/java/com/eventhub
├── controller
├── exception
├── infrastructure
├── mapper
├── security
├── service/impl
└── support
```

Main tested areas:

- authentication and login;
- JWT generation and validation;
- current user provider;
- service-layer business rules;
- category management;
- location management;
- organizer application workflow;
- event creation, update, publishing and cancellation;
- ticket type management;
- ticket purchase logic;
- registration cancellation;
- payment state changes;
- review creation, update and deletion;
- rating recalculation;
- user blocking and unblocking;
- statistics calculation;
- controller endpoints;
- global exception handling;
- mappers;
- AOP logging;
- event status scheduler.

### Run Tests

For Windows:

```bash
.\mvnw.cmd test
```

For Linux/macOS:

```bash
./mvnw test
```

### Generate JaCoCo Report

For Windows:

```bash
.\mvnw.cmd clean verify
```

For Linux/macOS:

```bash
./mvnw clean verify
```

JaCoCo report will be generated at:

```text
target/site/jacoco/index.html
```

Current total instruction coverage:

```text
84%
```

---

## JavaDoc

JavaDoc can be generated for project classes and methods.

For Windows:

```bash
.\mvnw.cmd javadoc:javadoc
```

For Linux/macOS:

```bash
./mvnw javadoc:javadoc
```

Generated documentation will be available at:

```text
target/site/apidocs/index.html
```

---

## Useful Docker Commands

### Start containers

```bash
docker compose up
```

### Start containers in detached mode

```bash
docker compose up -d
```

### Stop containers

```bash
docker compose down
```

### Stop containers and remove database volume

```bash
docker compose down -v
```

Use this command if you want to completely recreate the database and run Flyway migrations again from the beginning.

### Pull images from DockerHub

```bash
docker compose pull
```

### View running containers

```bash
docker compose ps
```

### View application logs

```bash
docker compose logs -f app
```

### View PostgreSQL logs

```bash
docker compose logs -f postgres
```

### Rebuild and push application image manually

```bash
docker build -t iamm1st/eventhub:latest .
docker push iamm1st/eventhub:latest
```

---

## HTTP Status Codes

The API uses standard HTTP status codes.

| Status Code | Meaning |
|---|---|
| `200 OK` | Request completed successfully. |
| `201 Created` | Resource was created successfully. |
| `204 No Content` | Request completed successfully, response body is empty. |
| `400 Bad Request` | Invalid request parameters or validation error. |
| `401 Unauthorized` | Authentication is required or token is invalid. |
| `403 Forbidden` | User is authenticated but does not have enough permissions. |
| `404 Not Found` | Requested resource was not found. |
| `409 Conflict` | Business conflict, for example duplicate data or forbidden state transition. |
| `415 Unsupported Media Type` | Request content type is not supported. |
| `500 Internal Server Error` | Unexpected server error. |

### Error Response Example

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Event not found with id: 100",
  "path": "/api/events/100",
  "timestamp": "2026-07-14T12:00:00"
}
```

### Validation Error Response Example

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/events",
  "timestamp": "2026-07-14T12:00:00",
  "errors": {
    "title": "Title is required",
    "capacity": "Capacity must be greater than 0"
  }
}
```

---

## Security Notes

The project includes several security-related mechanisms:

- stateless JWT authentication;
- BCrypt password hashing;
- role-based authorization;
- method-level security with `@PreAuthorize`;
- protected Actuator endpoints;
- blocked user checks;
- JSON responses for `401 Unauthorized` and `403 Forbidden`;
- global exception handling;
- DTO validation;
- database constraints;
- JPA repositories and parameterized JPQL queries instead of raw string SQL concatenation.

---

## Demonstration Scenario

A typical demonstration flow for the project:

1. Open Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

2. Login as admin:

```http
POST /api/auth/login
```

```json
{
  "email": "admin@eventhub.com",
  "password": "admin123"
}
```

3. Register a regular user.
4. Login as the regular user and submit an organizer application.
5. Login as admin and approve the organizer application.
6. Login again as the approved organizer.
7. Create location or use existing location.
8. Create event.
9. Create ticket type for event.
10. Publish event.
11. Register another user.
12. Buy ticket as regular user.
13. View registration and payment.
14. After event is finished, create review.
15. Check event rating recalculation.
16. Open admin statistics and audit logs.
17. Check JaCoCo report and test coverage.

---

## Possible Future Improvements

Possible future improvements for the project:

- refresh token support;
- real payment provider integration;
- email notifications;
- organizer dashboard frontend;
- admin moderation panel frontend;
- advanced event recommendation system;
- event image upload;
- ticket QR code generation;
- Docker image versioning with release tags;
- CI/CD pipeline with GitHub Actions;
- OWASP Dependency Check integration;
- integration tests with Testcontainers.

---

## Project Summary

EventHub demonstrates a complete Spring Boot REST API project with real business logic, role-based security, normalized database structure, Flyway migrations, DTO validation, custom exception handling, AOP audit logging, transaction management, pessimistic locking, Swagger documentation, Docker support and automated tests with JaCoCo coverage above 80%.

## Possible Future Improvements

In the future, EventHub can be improved by adding a frontend application, real payment provider integration, email notifications, refresh tokens, QR codes for tickets and more advanced analytics for organizers and administrators.
The project can also be extended with CI/CD pipelines, cloud deployment, stronger security mechanisms, and additional integration or end-to-end tests.