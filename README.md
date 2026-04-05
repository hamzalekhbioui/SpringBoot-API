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
| Redis                  | Caching layer with TTL support         |
| Bucket4j               | Token bucket rate limiting per IP      |
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

## Rate Limiting

The API uses **Bucket4j** with the **token bucket algorithm** to limit requests per IP address.

### How It Works

Each IP address gets a bucket with a fixed number of tokens (default: 10). Every request consumes one token. Tokens refill at a steady rate (default: 10 tokens per minute). When the bucket is empty, the request is rejected with `429 Too Many Requests`.

```
Request #1  → bucket: 9/10  → 200 OK
Request #2  → bucket: 8/10  → 200 OK
...
Request #10 → bucket: 0/10  → 200 OK
Request #11 → bucket: 0/10  → 429 Too Many Requests (Retry-After: 42)
```

### Configuration (`application.properties`)

```properties
rate-limit.capacity=10          # Max requests per window
rate-limit.refill-minutes=1     # Window duration in minutes
```

### Response Headers

Every response includes rate limit info:

| Header                   | Description                              |
|--------------------------|------------------------------------------|
| `X-Rate-Limit-Remaining` | Tokens left in the current window        |
| `Retry-After`            | Seconds to wait (only on 429 responses)  |

### 429 Response Example

```json
{
  "status": 429,
  "message": "Rate limit exceeded. Try again in 42 seconds.",
  "errors": null
}
```

### Testing with Postman

1. Send 10 rapid requests to `GET /tasks` (with a valid JWT)
2. The 11th request returns **429 Too Many Requests**
3. Check the `X-Rate-Limit-Remaining` header decreasing with each request
4. Wait for the `Retry-After` duration, then requests work again

Or test with curl in a loop:
```bash
for i in $(seq 1 12); do
  echo "Request $i: $(curl -s -o /dev/null -w '%{http_code}' \
    -H 'Authorization: Bearer <your-token>' \
    http://localhost:8081/tasks)"
done
```

### Excluded Endpoints

Swagger UI, API docs, H2 console, and WebSocket are **not rate-limited**.

### Distributed Rate Limiting

The current implementation uses in-memory buckets (per JVM instance). For distributed deployments with multiple app instances, you can upgrade to `bucket4j-redis` to store bucket state in Redis — ensuring consistent rate limits across all instances.

---

## Frontend (React)

A single-page application built with **React 18 + Vite** that consumes the API.

### Pages
- **Login** — Authenticate with username/password, stores JWT in localStorage
- **Register** — Create a new account, auto-login after registration
- **Tasks** — Full task management dashboard

### Features
- Create, edit, and delete tasks via modal forms
- Change task status with inline dropdown
- Filter by status (ALL / TODO / IN_PROGRESS / DONE)
- Pagination controls
- Real-time notifications via WebSocket (toast when another user modifies a task)
- Role-aware UI (delete button only visible to ADMIN users)
- Responsive design (mobile-friendly)

### Run in Development

```bash
cd frontend
npm install
npm run dev
```

Opens at **http://localhost:5173** (auto-proxies API calls to `localhost:8081`).

### Run with Docker

```bash
docker compose up --build
```

- Frontend: **http://localhost:3000**
- API: **http://localhost:8081**
- Swagger: **http://localhost:8081/swagger-ui/index.html**

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
- **Redis Caching** — `@Cacheable` on reads, `@CacheEvict` on writes, with configurable TTL (5 min for task lists, 10 min for single tasks)
- **Rate Limiting** — Bucket4j token bucket algorithm, 10 requests/minute per IP, configurable via properties, returns 429 with `Retry-After` header
- **React Frontend** — Login/Register, task CRUD, status filtering, pagination, real-time updates via WebSocket
- **WebSocket (STOMP)** — Real-time task notifications on create, update, and delete via `/topic/tasks`
- **Proper HTTP Status Codes** — 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 429 Too Many Requests

---

## Roadmap

- [x] **Swagger/OpenAPI Documentation** — Interactive API docs with `springdoc-openapi`
- [x] **JWT Authentication** — Spring Security with role-based access control (admin vs. regular user)
- [x] **Database Migrations** — Flyway replacing `ddl-auto=update`, with `createdAt`/`updatedAt` audit fields
- [x] **WebSocket Support** — Real-time task update notifications via STOMP
- [x] **Redis Caching** — `@Cacheable`/`@CacheEvict` with TTL configuration
- [x] **Rate Limiting** — Bucket4j token bucket per IP with configurable capacity
- [x] **Frontend Client** — React SPA with login, task CRUD, filtering, pagination, and real-time WebSocket updates

---
