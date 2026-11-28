# FlowDesk – Organization & Project Management System

FlowDesk is a multi-tenant project management and team collaboration backend built with **Java and Spring Boot**. It helps organizations manage their team members, customers, projects, and tasks through a secure RESTful API with role-based access control.

The system provides organization-level data isolation, invitation-based user onboarding, and task assignment workflows.

## Key Features

### 1. Authentication & Authorization
- JWT-based authentication.
- Secure password hashing using Spring Security.
- Role-based access control (RBAC).
- Protected REST API endpoints.
- Organization-level access control.

### 2. Organization & User Management
- Organization-based user management.
- Create and manage organization members.
- Support for three roles:
    - `OWNER`
    - `ADMIN`
    - `AGENT`
- User status management.
- Invitation-based onboarding.
- Email invitations with secure, expiring tokens.
- Resend invitation functionality.

### 3. Customer Management
- Create, retrieve, update, and delete customers.
- Associate customers with their organization.
- Search customers.
- Pagination support.

### 4. Project Management
- Create and manage projects.
- Associate projects with customers.
- Track project status, budget, start date, and due date.
- Search and paginate projects.
- Organization-scoped project access.

### 5. Task Management
- Create and manage tasks.
- Assign tasks to team members.
- Set task priority and status.
- Retrieve tasks by project.
- Allow agents to retrieve their assigned tasks.
- Restrict agents to updating the status of their own assigned tasks.

## Role-Based Permissions

| Feature | OWNER | ADMIN | AGENT |
|---|---|---|---|
| View projects | Yes | Yes | Yes |
| Create projects | Yes | Yes | No |
| Update projects | Yes | Yes | No |
| Delete projects | Yes | Yes | No |
| View customers | Yes | Yes | Yes |
| Create customers | Yes | Yes | No |
| Update customers | Yes | Yes | No |
| Delete customers | Yes | Yes | No |
| Manage users | Yes | Yes | No |
| Update user status | Yes | No | No |
| Resend invitations | Yes | No | No |
| Manage tasks | Yes | Yes | Limited |

**Note:** Permissions are enforced through Spring Security and business rules in the service layer. Agents have restricted task access and can update the status of tasks assigned to them.

## Tech Stack

### Backend
- Java 25
- Spring Boot 4.1.2
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- Jakarta Bean Validation
- JWT Authentication
- Maven

### Database & Persistence
- PostgreSQL
- Flyway Database Migrations

### Other Tools
- Lombok
- Email integration using Spring Mail
- RESTful API architecture

## Architecture

The application follows a layered architecture to separate responsibilities and maintainability.

```text
src/main/java/com/flowdesk
│
├── controller/       # REST API endpoints
├── service/          # Business logic
├── repository/       # Database operations
├── entity/           # JPA entities
├── dto/              # Request and response objects
├── security/         # JWT and authentication
├── exception/        # Custom exceptions and error handling
└── enums/            # Application enums
```

### Request Flow

```text
Client
  |
  v
Controller
  |
  v
Service Layer
  |
  v
Repository
  |
  v
PostgreSQL Database
```

## Getting Started

### Prerequisites

Make sure you have the following installed:

- Java 25
- PostgreSQL 17 or compatible version
- Git
- Maven (optional, since the project includes Maven Wrapper)

### 1. Clone the Repository

```bash
git clone <YOUR_REPOSITORY_URL>
cd flowdesk-backend
```

### 2. Create the Database

Create a PostgreSQL database:

```sql
CREATE DATABASE flowdesk_db;
```

Configure your database connection in `application.yml`.

Example:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5436/flowdesk_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate
```

### 3. Configure Environment Variables

The application requires environment variables for sensitive configuration.

| Variable | Description |
|---|---|
| `JWT_SECRET` | Secret key used for JWT signing |
| `MAIL_USERNAME` | SMTP sender email |
| `MAIL_PASSWORD` | SMTP app password |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |

For Gmail SMTP, use a Google App Password instead of your regular Gmail password.

Example using PowerShell:

```powershell
$env:JWT_SECRET="YOUR_SECURE_JWT_SECRET"
$env:MAIL_USERNAME="reemsina2@gmail.com"
$env:MAIL_PASSWORD="rqzt xvzz lptn gbwi"

$env:DB_USERNAME="flowdesk"
$env:DB_PASSWORD="YOUR_DATABASE_PASSWORD"
```

Keep all credentials private. Never commit real secrets to GitHub.

### 4. Run the Application

Using Maven Wrapper:

**Windows:**
```powershell
.\mvnw spring-boot:run
```

**Linux / macOS:**
```bash
./mvnw spring-boot:run
```

The backend runs at:

```text
http://localhost:8080
```

Flyway manages database migrations during application startup.

## API Modules

The backend exposes RESTful endpoints organized by resource:

| Module | Base Endpoint | Description |
|---|---|---|
| Users | `/api/users` | User and invitation management |
| Projects | `/api/projects` | Project management |
| Customers | `/api/customers` | Customer management |
| Tasks | `/api/tasks` | Task management |

### Example API Requests

**Get all projects:**

```http
GET /api/projects
Authorization: Bearer <JWT_TOKEN>
```

**Create a project:**

```http
POST /api/projects
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

```json
{
  "name": "Company Website",
  "description": "Building a company website",
  "customerId": 2,
  "status": "PLANNING"
}
```

**Get assigned tasks:**

```http
GET /api/tasks/my-tasks
Authorization: Bearer <AGENT_JWT_TOKEN>
```

**Update an assigned task status:**

```http
PATCH /api/tasks/{id}/status
Authorization: Bearer <AGENT_JWT_TOKEN>
Content-Type: application/json
```

```json
{
  "status": "IN_PROGRESS"
}
```

## Security

Security is an important part of FlowDesk.

The application implements:

- JWT authentication for protected endpoints.
- Role-based authorization using Spring Security.
- Organization-level data isolation.
- Password hashing.
- Secure invitation tokens stored as hashes.
- Invitation expiration and single-use validation.
- Service-layer authorization checks for sensitive operations.
- Restricted agent access to assigned tasks.

## Current Development Status

The following modules have been implemented and manually tested during development:

- Authentication and JWT authorization.
- Organization and user management.
- Invitation and email activation workflow.
- Customer management.
- Project management and search.
- Task management and agent task assignment.
- Role-based permissions for OWNER, ADMIN, and AGENT.

Further work includes comprehensive automated testing, additional security review, API documentation, and production deployment.

## Future Improvements

- Automated unit and integration tests.
- API documentation with Swagger / OpenAPI.
- Frontend integration.
- Dashboard analytics and reporting.
- Notifications.
- Production deployment and monitoring.

---

**FlowDesk** – A secure, organization-based project and team management backend built with Java Spring Boot.