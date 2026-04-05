# Task Manager REST API

A production-ready Task Management REST API built with **Spring Boot 3**, **Java 21**, and **Spring Data JPA**. Features JWT authentication, role-based access control, Swagger docs, comprehensive error handling, validation, filtering, and pagination.

```

**Layers:**
- **Controller** — REST endpoints, request validation, HTTP status mapping
- **Service** — Business logic, partial/full update handling
- **Repository** — Data access via Spring Data JPA with custom query methods
- **Security** — JWT authentication filter, role-based authorization
- **Exception Handling** — `@RestControllerAdvice` with structured error responses

---

## Tech Stack

| Technology             | Purpose                                |
|------------------------|----------------------------------------|
| Java 21                | Language                               |
| Spring Boot 3.2.5      | Framework                              |
| Spring Security        | Authentication & Authorization         |
| JWT (jjwt 0.12.5)      | Stateless token-based authentication   |
| Spring Data JPA        | ORM & Data Access                      |
| Flyway                 | Database migrations & schema versioning|
| WebSocket (STOMP)      | Real-time task update notifications    |
| H2 Database            | In-memory database                     |
| springdoc-openapi       | Swagger UI & OpenAPI 3 documentation   |
| Bean Validation        | Input validation (`@NotBlank`, `@Size`) |
| Lombok                 | Boilerplate reduction                  |
| JUnit 5 + Mockito      | Unit & integration testing             |

---

## Authentication

The API uses **JWT (JSON Web Tokens)** for stateless authentication with **role-based access control**.

### Roles
| Role    | Permissions                                    |
|---------|------------------------------------------------|
| `USER`  | Create, read, update tasks                     |
| `ADMIN` | Full access (create, read, update, **delete**)  |

### Auth Endpoints

| Method | Endpoint              | Description              | Auth Required |
|--------|-----------------------|--------------------------|---------------|
| `POST` | `/auth/register`      | Register as USER         | No            |
| `POST` | `/auth/register/admin`| Register as ADMIN        | No            |
| `POST` | `/auth/login`         | Login & get JWT token    | No            |



## Task Endpoints

Base URL: `http://localhost:8081/tasks` (all require JWT authentication)

| Method   | Endpoint      | Description                       | Role Required | Status Codes       |
|----------|---------------|-----------------------------------|---------------|--------------------|
| `GET`    | `/tasks`      | List all tasks (paginated, filterable) | USER, ADMIN | 200, 400, 401 |
| `GET`    | `/tasks/{id}` | Get task by ID                    | USER, ADMIN   | 200, 401, 404      |
| `POST`   | `/tasks`      | Create a new task                 | USER, ADMIN   | 201, 400, 401      |
| `PUT`    | `/tasks/{id}` | Full update                       | USER, ADMIN   | 200, 400, 401, 404 |
| `PATCH`  | `/tasks/{id}` | Partial update                    | USER, ADMIN   | 200, 401, 404      |
| `DELETE` | `/tasks/{id}` | Delete a task                     | **ADMIN only**| 204, 401, 403, 404 |

### Query Parameters (GET /tasks)

| Parameter | Type   | Required | Description                              |
|-----------|--------|----------|------------------------------------------|
| `status`  | String | No       | Filter by status: `TODO`, `IN_PROGRESS`, `DONE` |
| `page`    | int    | No       | Page number (default: 0)                 |
| `size`    | int    | No       | Page size (default: 20)                  |

---



## WebSocket — Real-Time Updates

The API broadcasts task events over **STOMP WebSocket**. Any connected client receives instant notifications when tasks are created, updated, or deleted.

### Connection

| Property          | Value                              |
|-------------------|------------------------------------|
| Endpoint          | `ws://localhost:8081/ws` (SockJS)  |
| Subscribe topic   | `/topic/tasks`                     |

### Event Payload

```json
{
  "action": "CREATED",
  "task": {
    "id": 1,
    "title": "New task",
    "description": null,
    "status": "TODO",
    "createdAt": "2026-04-05T19:30:00",
    "updatedAt": "2026-04-05T19:30:00"
  }
}
```

**Actions:** `CREATED`, `UPDATED`, `DELETED`

### JavaScript Client Example

```javascript
const socket = new SockJS('http://localhost:8081/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  stompClient.subscribe('/topic/tasks', (message) => {
    const event = JSON.parse(message.body);
    console.log(event.action, event.task);
  });
});
```

---

## Features

- **JWT Authentication** — Stateless token-based auth with 24h expiration
- **Role-Based Access Control** — USER and ADMIN roles with endpoint-level authorization
- **Full CRUD** — Create, Read, Update (PUT & PATCH), Delete
- **Swagger/OpenAPI** — Interactive API docs with JWT bearer auth support
- **Validation** — `@NotBlank` on title, `@Size(max=500)` on description, structured error responses
- **Global Exception Handling** — `@RestControllerAdvice` catches `TaskNotFoundException`, validation errors, malformed JSON, and type mismatches
- **Filtering** — Filter tasks by status enum (`TODO`, `IN_PROGRESS`, `DONE`)
- **Pagination** — Spring Data `Pageable` with page/size parameters
- **Database Migrations** — Flyway-managed schema with versioned SQL scripts (no `ddl-auto=update`)
- **Audit Fields** — `createdAt` and `updatedAt` timestamps on all entities, auto-managed via JPA lifecycle callbacks
- **WebSocket (STOMP)** — Real-time task notifications on create, update, and delete via `/topic/tasks`
- **Proper HTTP Status Codes** — 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found

---

## Roadmap

- [x] **Swagger/OpenAPI Documentation** — Interactive API docs with `springdoc-openapi`
- [x] **JWT Authentication** — Spring Security with role-based access control (admin vs. regular user)
- [x] **Database Migrations** — Flyway replacing `ddl-auto=update`, with `createdAt`/`updatedAt` audit fields
- [x] **WebSocket Support** — Real-time task update notifications via STOMP
- [ ] **Redis Caching** — Cache frequently accessed data
- [ ] **Rate Limiting** — Protect API from abuse
- [ ] **Frontend Client** — React or Angular UI consuming the API

---
