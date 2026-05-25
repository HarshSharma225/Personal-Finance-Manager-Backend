# Personal Finance Manager

A REST API built with Spring Boot for managing personal finances — tracking income, expenses, and savings goals.

## Tech Stack

 - Java 25
- Spring Boot 3.2
- Spring Security (session-based auth)
- Spring Data JPA
- H2 In-Memory Database
- Maven
- JUnit 5 + Mockito

## Getting Started

### Prerequisites
 - Java 25+
 - Maven 4.0+

### Run Locally

```bash
cd finance-manager
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api`

### Run Tests

```bash
mvn test
```

## Deployment (Render)

1. Push code to a public GitHub repository
2. Go to [render.com](https://render.com) and create a new **Web Service**
3. Connect your GitHub repository
4. Set the following:
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/finance-manager-0.0.1-SNAPSHOT.jar`
   - **Environment**: Java
5. Deploy

## API Documentation

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register new user |
| POST | /api/auth/login | Login |
| POST | /api/auth/logout | Logout |

#### Register
```
POST /api/auth/register
{
  "username": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890"
}
```

#### Login
```
POST /api/auth/login
{
  "username": "user@example.com",
  "password": "password123"
}
```

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/transactions | Create transaction |
| GET | /api/transactions | Get all transactions |
| PUT | /api/transactions/{id} | Update transaction |
| DELETE | /api/transactions/{id} | Delete transaction |

Query params for GET: `startDate`, `endDate`, `categoryId`

### Categories

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/categories | Get all categories |
| POST | /api/categories | Create custom category |
| DELETE | /api/categories/{name} | Delete custom category |

Default categories: Salary (INCOME), Food, Rent, Transportation, Entertainment, Healthcare, Utilities (all EXPENSE)

### Savings Goals

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/goals | Create goal |
| GET | /api/goals | Get all goals |
| GET | /api/goals/{id} | Get goal by ID |
| PUT | /api/goals/{id} | Update goal |
| DELETE | /api/goals/{id} | Delete goal |

### Reports

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/reports/monthly/{year}/{month} | Monthly report |
| GET | /api/reports/yearly/{year} | Yearly report |

## Design Decisions

- **Session-based auth**: Used Spring Security's default session mechanism with cookies. Simple and works well for this use case.
- **H2 in-memory DB**: Easy to set up and deploy without needing an external database. Data resets on restart which is fine for a demo.
- **Layered architecture**: Controller → Service → Repository. Keeps things clean and testable.
- **Global exception handler**: All errors go through one place so responses are consistent.
