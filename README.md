# Task Manager REST API

A production-ready Task Management REST API built with **Spring Boot 3**, **Java 21**, and **Spring Data JPA**. Designed with clean layered architecture, comprehensive error handling, validation, filtering, and pagination.

---

## Architecture

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Client     │────▶│  Controller  │────▶│   Service    │────▶│  Repository  │
│  (REST)      │◀────│  (Validation)│◀────│  (Business)  │◀────│  (JPA/H2)    │
└──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘
                            │
                     ┌──────▼───────┐
                     │   Global     │
                     │  Exception   │
                     │   Handler    │
                     └──────────────┘
```

**Layers:**
- **Controller** — REST endpoints, request validation, HTTP status mapping
- **Service** — Business logic, partial/full update handling
- **Repository** — Data access via Spring Data JPA with custom query methods
- **Exception Handling** — `@RestControllerAdvice` with structured error responses

---

## Tech Stack

| Technology         | Purpose                          |
|--------------------|----------------------------------|
| Java 21            | Language                         |
| Spring Boot 3.2.5  | Framework                        |
| Spring Data JPA    | ORM & Data Access                |
| H2 Database        | In-memory database               |
| Bean Validation    | Input validation (`@NotBlank`, `@Size`) |
| Lombok             | Boilerplate reduction            |
| JUnit 5 + Mockito  | Unit & integration testing       |

---

## API Endpoints

Base URL: `http://localhost:8081/tasks`

| Method   | Endpoint      | Description             | Status Codes       |
|----------|---------------|-------------------------|-------------------|
| `GET`    | `/tasks`      | List all tasks (paginated, filterable) | 200, 400 |
| `GET`    | `/tasks/{id}` | Get task by ID          | 200, 404           |
| `POST`   | `/tasks`      | Create a new task       | 201, 400           |
| `PUT`    | `/tasks/{id}` | Full update             | 200, 400, 404      |
| `PATCH`  | `/tasks/{id}` | Partial update          | 200, 404           |
| `DELETE` | `/tasks/{id}` | Delete a task           | 204, 404           |

### Query Parameters (GET /tasks)

| Parameter | Type   | Required | Description                              |
|-----------|--------|----------|------------------------------------------|
| `status`  | String | No       | Filter by status: `TODO`, `IN_PROGRESS`, `DONE` |
| `page`    | int    | No       | Page number (default: 0)                 |
| `size`    | int    | No       | Page size (default: 20)                  |

---


cd taskmanager
./mvnw spring-boot:run
```
The API starts at **http://localhost:8081**.

### Swagger UI
Open **http://localhost:8081/swagger-ui/index.html** for interactive API documentation.

### Access H2 Console
Navigate to `http://localhost:8081/h2-console` with:
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** *(empty)*

### Run Tests
```bash
./mvnw test
```
**Test coverage:** 36 tests (21 controller + 15 service) covering all endpoints, validation, error handling, and edge cases.

---

## Features

- **Full CRUD** — Create, Read, Update (PUT & PATCH), Delete
- **Validation** — `@NotBlank` on title, `@Size(max=500)` on description, structured error responses
- **Global Exception Handling** — `@RestControllerAdvice` catches `TaskNotFoundException`, validation errors, malformed JSON, and type mismatches
- **Filtering** — Filter tasks by status enum (`TODO`, `IN_PROGRESS`, `DONE`)
- **Pagination** — Spring Data `Pageable` with page/size parameters
- **Proper HTTP Status Codes** — 201 for creation, 204 for deletion, 404 for not found, 400 for validation errors

---

## Roadmap

- [x] **Swagger/OpenAPI Documentation** — Interactive API docs with `springdoc-openapi`
- [ ] **JWT Authentication** — Spring Security with role-based access control (admin vs. regular user)
- [ ] **Database Migrations** — Flyway/Liquibase replacing `ddl-auto=update`, with `createdAt`/`updatedAt` audit fields
- [ ] **WebSocket Support** — Real-time task update notifications
- [ ] **Redis Caching** — Cache frequently accessed data
- [ ] **Rate Limiting** — Protect API from abuse
- [ ] **Frontend Client** — React or Angular UI consuming the API

