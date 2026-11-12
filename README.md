# Banking System (Spring Boot REST API and Web App)

A banking application built with Java and Spring Boot, with a web page on top. It supports user registration and login with JWT, role-based access (customer and admin), accounts, deposits, withdrawals, money transfers and a passbook-style transaction history.

**Live app:** https://banking-system-r1q8.onrender.com (free hosting, the first request may take about a minute to wake up)

**API docs (Swagger UI):** https://banking-system-r1q8.onrender.com/swagger-ui/index.html

## Tech Stack
- Java 21
- Spring Boot, Spring Data JPA (Hibernate)
- Spring Security with JWT (OAuth2 resource server), BCrypt password hashing
- PostgreSQL (Neon) in production, H2 in-memory database for local development
- Web frontend in plain HTML, CSS and JavaScript, served by Spring Boot
- Swagger UI (springdoc-openapi)
- JUnit 5 and Mockito for unit tests
- Maven, Docker
- Deployed on Render

## Features
- User registration and login, JWT access tokens (1 hour expiry), BCrypt-hashed passwords
- Role-based access: customers can only access their own accounts, admins can access all accounts
- Create a bank account with an initial deposit
- Deposit and withdraw money
- Transfer money between accounts (atomic, rolls back on failure using `@Transactional`)
- Optimistic locking to handle concurrent updates
- Input validation and global exception handling
- Transaction history for every account
- Web app with sign in, account list, deposit, withdraw, transfer and a passbook-style transaction history with running balance
- Persistent data stored in a managed PostgreSQL database
- Unit tests for account creation, deposits, withdrawals and transfers (including failure cases)

## How Authentication Works
1. `POST /api/auth/register` creates a customer user.
2. `POST /api/auth/login` returns a JWT token.
3. Send the token in the `Authorization: Bearer <token>` header on every other request. The web app does this automatically after you sign in.
4. In Swagger UI, click **Authorize** and paste the token.

Without a token the API returns `401`. Accessing another user's account returns `403`.

## API Endpoints

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/register` | Public | Register a customer |
| POST | `/api/auth/login` | Public | Login and get a JWT |
| POST | `/api/accounts` | Logged in | Create an account for the current user |
| GET | `/api/accounts` | Logged in | List my accounts |
| GET | `/api/accounts/{number}` | Owner or admin | Get account details |
| POST | `/api/accounts/{number}/deposit` | Owner or admin | Deposit money |
| POST | `/api/accounts/{number}/withdraw` | Owner or admin | Withdraw money |
| POST | `/api/transfers` | Owner of source account or admin | Transfer between accounts |
| GET | `/api/accounts/{number}/transactions` | Owner or admin | Transaction history |
| GET | `/api/admin/accounts` | Admin only | List all accounts |

### Example: Create account
`POST /api/accounts`
```json
{
  "holderName": "Ravi",
  "initialDeposit": 5000
}
```

### Example: Transfer
`POST /api/transfers`
```json
{
  "fromAccount": "SB1234567890",
  "toAccount": "SB0987654321",
  "amount": 700
}
```

## How to Run Locally
1. Install JDK 21 or higher
2. Clone the repository:
   ```
   git clone https://github.com/etteyjayadeep2005-bit/banking-system.git
   ```
3. Go into the folder and run (on Windows use `mvnw.cmd spring-boot:run`):
   ```
   ./mvnw spring-boot:run
   ```
4. Open `http://localhost:8080` for the web app. Swagger UI is at `http://localhost:8080/swagger-ui/index.html`

By default the app uses an in-memory H2 database locally. Optional environment variables:
- `DB_URL` (for example `jdbc:postgresql://HOST/DBNAME?sslmode=require`), `DB_USER`, `DB_PASSWORD` to use PostgreSQL
- `JWT_SECRET` to sign tokens (set a long random value in production)
- `ADMIN_USERNAME` (default `admin`) and `ADMIN_PASSWORD` to create an admin user at startup. No admin is created if `ADMIN_PASSWORD` is not set.

## Run Tests
```
./mvnw test
```
(On Windows use `mvnw.cmd test`.)

## Roadmap
- Integration tests for the REST endpoints