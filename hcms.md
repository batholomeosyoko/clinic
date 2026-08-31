# Hospital / Clinic Management System — Project Documentation

**Stack:** Spring Boot · Spring Data JPA · PostgreSQL · Spring Security (JWT) · Docker
**Author:** Batholomeo Syoko
**Status:** In development (learning project — backend mastery series)

---

## 1. Overview

The Hospital/Clinic Management System (HCMS) is a backend REST API that digitizes the core operations of a small clinic or hospital: registering patients, managing doctors, booking appointments without double-booking, issuing prescriptions, and tracking billing.

It is built as a real, production-shaped system rather than a toy CRUD app — meaning it includes authentication, role-based access control, input validation, proper error handling, automated tests, and a deployment pipeline. The goal of the project is to master Spring Boot fundamentals through a domain complex enough to require real architectural decisions.

### 1.1 Who this document is for

This documentation is written so that anyone — a fellow student, a recruiter, a teammate joining the project, or a future version of the author — can understand what the system does, how it is built, and how to run it, without needing prior context.

---

## 2. Goals and Non-Goals

**Goals**
- Provide a working backend API for core hospital operations
- Demonstrate correct handling of relationships, security, and business rules (e.g. appointment conflicts)
- Be cleanly layered (controller → service → repository) and easy to extend
- Be deployable and testable, not just runnable on a local machine

**Non-goals (for this version)**
- No frontend UI (this is an API-only project; a UI could consume it later)
- No real payment processor integration (billing status is tracked, not charged)
- No multi-clinic / multi-tenant support (single clinic scope)

---

## 3. User Roles

The system supports four roles, each with different permissions:

| Role | Description | Example permissions |
|---|---|---|
| **Admin** | Manages the system and staff | Create/remove doctors, view all data, manage billing |
| **Doctor** | Clinical staff member | View own schedule, write prescriptions, update appointment outcomes |
| **Receptionist** | Front-desk staff | Register patients, book/reschedule appointments, manage billing |
| **Patient** | End user of the clinic | View own appointments and prescriptions, request bookings |

Access is enforced at the API level — a user can only see and modify data appropriate to their role.

---

## 4. Domain Model

### 4.1 Core Entities

- **User** — login identity, linked to a Role (`ADMIN`, `DOCTOR`, `RECEPTIONIST`, `PATIENT`)
- **Patient** — personal and medical-contact details, linked to a User
- **Doctor** — specialty, availability, linked to a User
- **Appointment** — links a Patient and a Doctor at a specific time slot, with a status (`PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`)
- **Prescription** — issued by a Doctor after a completed Appointment, lists medication and instructions
- **Billing** — tied to an Appointment or Patient, tracks amount and payment status (`PENDING`, `PAID`)

### 4.2 Relationships

```
User (1) ── (1) Patient
User (1) ── (1) Doctor

Doctor (1) ── (many) Appointment
Patient (1) ── (many) Appointment

Appointment (1) ── (0..1) Prescription
Appointment (1) ── (0..1) Billing
```

A Doctor cannot have two overlapping Appointments — this is enforced in the service layer before any booking is saved, which is the most business-critical rule in the system.

---

## 5. System Architecture

The application follows a standard layered architecture:

```
Controller Layer   → handles HTTP requests, input validation (DTOs)
Service Layer      → business logic (e.g. conflict detection, role checks)
Repository Layer   → Spring Data JPA interfaces, database access
Entity Layer        → JPA-mapped domain objects
```

**Why this matters:** Controllers never talk to the database directly, and business rules never live in controllers. This separation makes the codebase testable (services can be tested without HTTP) and easier to extend later (e.g. swapping the database or adding a message queue doesn't touch controllers).

### 5.1 Cross-Cutting Concerns

- **Security:** Spring Security with JWT-based stateless authentication
- **Validation:** Bean Validation (`@Valid`, `@NotNull`, etc.) on all incoming DTOs
- **Error Handling:** a centralized `@ControllerAdvice` returns consistent, structured error responses
- **API Documentation:** auto-generated via springdoc-openapi (Swagger UI)

---

## 6. API Design

All endpoints are prefixed with `/api/v1`. Below is a representative (not exhaustive) list.

### 6.1 Authentication

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/auth/register` | Register a new user | Public |
| POST | `/auth/login` | Log in, receive a JWT | Public |

### 6.2 Patients

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/patients` | Register a new patient | Admin, Receptionist |
| GET | `/patients/{id}` | View a patient's profile | Admin, Receptionist, Doctor, self (Patient) |
| GET | `/patients` | List/search patients (paginated) | Admin, Receptionist |
| PUT | `/patients/{id}` | Update patient details | Admin, Receptionist, self |

### 6.3 Doctors

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/doctors` | Add a new doctor | Admin |
| GET | `/doctors` | List doctors (filter by specialty) | All authenticated users |
| GET | `/doctors/{id}/availability` | View a doctor's open time slots | All authenticated users |

### 6.4 Appointments

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/appointments` | Book an appointment | Receptionist, Patient |
| GET | `/appointments/{id}` | View appointment details | Admin, Doctor (own), Patient (own) |
| GET | `/appointments` | List appointments (filter by date/doctor/status) | Role-scoped |
| PATCH | `/appointments/{id}/status` | Update status (confirm, complete, cancel) | Doctor, Receptionist |

Booking an overlapping slot for the same doctor returns `409 Conflict` with a clear error message.

### 6.5 Prescriptions & Billing

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/prescriptions` | Issue a prescription for a completed appointment | Doctor |
| GET | `/prescriptions/{patientId}` | View a patient's prescriptions | Doctor, Admin, self |
| POST | `/billing` | Create a billing record | Receptionist, Admin |
| PATCH | `/billing/{id}/pay` | Mark billing as paid | Receptionist, Admin |

Full, always-current endpoint documentation is available at `/swagger-ui.html` once the app is running.

---

## 7. Data Validation & Error Handling

- All request bodies are validated using Bean Validation annotations; invalid input returns `400 Bad Request` with a field-level error list.
- Domain-specific errors (e.g. booking conflicts, not-found resources) throw custom exceptions (`AppointmentConflictException`, `PatientNotFoundException`, etc.), caught centrally and translated into consistent JSON error responses with appropriate HTTP status codes.
- No stack traces or internal details are ever exposed to API clients.

Example error response shape:
```json
{
  "timestamp": "2026-08-31T10:15:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Doctor already has an appointment in this time slot",
  "path": "/api/v1/appointments"
}
```

---

## 8. Security Model

- Authentication is stateless, using signed JWTs issued at login.
- Every protected endpoint requires a valid `Authorization: Bearer <token>` header.
- Authorization is enforced with method-level checks (`@PreAuthorize`) tied to the user's role, and further scoped so, for example, a Patient can only ever access their own records — never another patient's.
- Passwords are hashed (BCrypt) and never stored or returned in plain text.

---

## 9. Database

- **Engine:** PostgreSQL
- **Schema management:** Flyway migrations (versioned SQL files), so the schema evolves in a tracked, repeatable way rather than relying on auto-generation in production.
- **Local development:** PostgreSQL runs via Docker Compose alongside the app, so the local environment matches production.

---

## 10. Testing Strategy

| Type | Tool | What it covers |
|---|---|---|
| Unit tests | JUnit 5 + Mockito | Service-layer business logic in isolation (e.g. conflict detection edge cases) |
| Integration tests | Testcontainers | Full request-to-database flow against a real, disposable PostgreSQL instance |
| API tests | Spring Boot Test (MockMvc) | Controller behavior, status codes, validation errors |

Testcontainers is used specifically because an in-memory database (like H2) can behave differently from PostgreSQL in ways that hide real bugs — testing against the real engine catches issues that would otherwise only appear in production.

---

## 11. Deployment

- **CI:** GitHub Actions runs the full test suite on every push
- **Hosting:** deployed to Render (or Railway) with environment-based configuration (no secrets committed to source control)
- **Documentation:** Swagger UI is exposed on the deployed instance for live, interactive API exploration

---

## 12. Running the Project Locally

```bash
# 1. Clone the repository
git clone <repository-url>
cd hospital-clinic-management-system

# 2. Start the database
docker compose up -d

# 3. Run the application
./mvnw spring-boot:run

# 4. Explore the API
open http://localhost:8080/swagger-ui.html
```

Environment variables (database URL, JWT secret, etc.) are supplied via a `.env` file or `application.yml` profile — see `application-example.yml` in the repository for the required keys.

---

## 13. Project Structure

```
src/main/java/com/batholomeo/hcms/
├── config/          # Security config, Swagger config, etc.
├── controller/      # REST controllers
├── dto/             # Request/response objects
├── entity/          # JPA entities
├── exception/       # Custom exceptions + global handler
├── repository/      # Spring Data JPA repositories
├── service/         # Business logic
└── HcmsApplication.java
```

---

## 14. Roadmap / Future Improvements

- Email/SMS reminders for upcoming appointments
- Doctor availability calendar with recurring schedules
- Audit logging for sensitive actions (e.g. record access)
- Multi-clinic (tenant) support

---

## 15. Glossary

- **DTO (Data Transfer Object):** an object used to move data between layers (e.g. API request/response) without exposing internal database entities directly.
- **JWT (JSON Web Token):** a compact, signed token used to prove a user's identity on each request without the server needing to store session state.
- **Testcontainers:** a testing library that spins up real services (like PostgreSQL) in Docker containers during test runs, so tests run against the real thing instead of a substitute.

---

*This document describes the intended design of the system and is updated as the implementation progresses.*