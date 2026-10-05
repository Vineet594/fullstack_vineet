# Secure Content Management & User Management System

Professional Spring Boot 3.x backend project that combines **all major college experiments** into one realistic application.

## Overview

A production-style REST API for managing users, roles, posts/content and categories with:

- JWT Authentication + Refresh Tokens
- Role-Based Access Control (ADMIN / USER)
- AES-GCM encryption for phone numbers
- Global exception handling
- Request logging + Correlation ID
- Pagination & Sorting
- N+1 Query Problem solved with EntityGraph / JOIN FETCH
- Spring Cache (Caffeine)
- Query optimization & indexes
- Swagger / OpenAPI documentation

## Features

| Feature                    | Implementation                          |
|---------------------------|-----------------------------------------|
| REST APIs                 | Controllers + proper status codes       |
| Exception Handling        | `@RestControllerAdvice`                 |
| Logging                   | RequestLoggingFilter + SLF4J            |
| Correlation ID            | CorrelationIdFilter + MDC               |
| Pagination & Sorting      | Spring Data `Pageable`                  |
| N+1 Solution              | `@EntityGraph` + JOIN FETCH             |
| Caching                   | Spring Cache + Caffeine                 |
| JWT + RBAC                | JwtAuthenticationFilter + `@PreAuthorize` |
| AES Encryption            | EncryptionService (AES/GCM)             |
| Access + Refresh Tokens   | AuthService + RefreshToken entity       |
| Swagger                   | springdoc-openapi                       |

## Technologies

- Java 17
- Spring Boot 3.3.4
- Spring Security + JWT (jjwt 0.12.6)
- Spring Data JPA + MySQL
- Caffeine Cache
- Lombok
- Springdoc OpenAPI
- BCrypt + AES-GCM

## How to Run

### Prerequisites
- JDK 17+
- MySQL 8 running on `localhost:3306`
- Maven 3.8+

### Steps

1. Create database (optional – auto-created):
   ```sql
   CREATE DATABASE secure_cms;
   ```

2. Update credentials in `src/main/resources/application.properties` if needed:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=yourpassword
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

4. Open Swagger UI:
   ```
   http://localhost:8080/swagger-ui.html
   ```

### Demo Credentials

| Role  | Username | Password  |
|-------|----------|-----------|
| ADMIN | admin    | admin123  |
| USER  | vineet   | user123   |
| USER  | rahul    | user123   |

## API Endpoints (Summary)

### Authentication
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

### Users (Admin)
- `GET /api/users` (paginated)
- `GET /api/users/{id}`
- `DELETE /api/users/{id}`

### Categories
- `GET /api/categories` (cached, public)
- `GET /api/categories/{id}`
- `POST /api/categories` (Admin)
- `PUT /api/categories/{id}` (Admin)
- `DELETE /api/categories/{id}` (Admin)

### Posts
- `GET /api/posts?page=0&size=10&sort=createdAt,desc` (public, JOIN FETCH)
- `GET /api/posts/{id}` (cached)
- `POST /api/posts` (authenticated)
- `PUT /api/posts/{id}` (owner or Admin)
- `DELETE /api/posts/{id}` (owner or Admin)

## Experiment Mapping (for Viva)

| Experiment Concept              | Where Implemented                          |
|--------------------------------|--------------------------------------------|
| REST API Design                | Controllers + ApiResponse                  |
| Exception Handling             | GlobalExceptionHandler (`@RestControllerAdvice`) |
| Request Logging                | RequestLoggingFilter                       |
| Correlation ID                 | CorrelationIdFilter + MDC                  |
| Pagination                     | Pageable + PageResponse                    |
| Sorting                        | Pageable sort parameter                    |
| N+1 Query Problem              | PostRepository `@EntityGraph`              |
| Caching                        | @Cacheable / @CacheEvict + Caffeine        |
| Query Optimization             | Indexes + JOIN FETCH + selective fetching  |
| JWT Authentication             | JwtService + JwtAuthenticationFilter       |
| RBAC                           | @PreAuthorize + Role entity                |
| AES Encryption                 | EncryptionService (AES/GCM)                |
| Access + Refresh Tokens        | AuthService + RefreshToken entity          |
| Swagger / OpenAPI              | OpenApiConfig + annotations                |

## Architecture

```
Controller → Service → Repository → Database
     ↑           ↑
 Security    Caching / Encryption
```

## Important Notes for Viva

1. **Authentication vs Authorization**
   - Authentication = Who are you? (JWT login)
   - Authorization = What can you do? (`@PreAuthorize`)

2. **Password vs AES**
   - Passwords → BCrypt (one-way hash)
   - Phone numbers → AES-GCM (reversible encryption)

3. **Why JOIN FETCH / EntityGraph?**
   - Avoids N+1: one query instead of 1 + N queries when loading posts with author & category.

4. **Correlation ID**
   - Helps debug requests across logs and microservices.

5. **Cache Invalidation**
   - `@CacheEvict` on create/update/delete keeps data consistent.

## Common Errors & Fixes

| Error | Solution |
|-------|----------|
| `Access denied` | Check JWT token and role |
| `Validation failed` | Check request body fields |
| `Token expired` | Use `/api/auth/refresh` |
| MySQL connection refused | Start MySQL & check credentials |
| LazyInitializationException | Already solved with EntityGraph |

## Future Improvements

- Redis instead of Caffeine for distributed cache
- Token blacklisting on logout
- Rate limiting
- File upload for post images
- Email verification

---

**Made for college project / viva demonstration.**
All experiments are integrated into one professional application.
