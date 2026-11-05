# Banking System (Spring Boot REST API)

A banking REST API built with Java and Spring Boot. It supports creating accounts, deposits, withdrawals, money transfers and transaction history.

**Live demo:** https://banking-system-r1q8.onrender.com (free hosting, the first request may take about a minute to wake up)

**API docs (Swagger UI):** https://banking-system-r1q8.onrender.com/swagger-ui/index.html

## Tech Stack
- Java 21
- Spring Boot, Spring Data JPA (Hibernate)
- PostgreSQL (Neon) in production, H2 in-memory database for local development
- Swagger UI (springdoc-openapi)
- Maven, Docker
- Deployed on Render

## Features
- Create a bank account with an initial deposit
- Deposit and withdraw money
- Transfer money between accounts (atomic, rolls back on failure using `@Transactional`)
- Optimistic locking to handle concurrent updates
- Input validation and global exception handling
- Transaction history for every account
- Persistent data stored in a managed PostgreSQL database

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/accounts` | Create a new account |
| GET | `/api/accounts/{number}` | Get account details |
| POST | `/api/accounts/{number}/deposit` | Deposit money |
| POST | `/api/accounts/{number}/withdraw` | Withdraw money |
| POST | `/api/transfers` | Transfer between accounts |
| GET | `/api/accounts/{number}/transactions` | Transaction history |

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
4. The API starts at `http://localhost:8080` and Swagger UI is at `http://localhost:8080/swagger-ui/index.html`

By default the app uses an in-memory H2 database locally. To use PostgreSQL, set these environment variables before running:
- `DB_URL` (for example `jdbc:postgresql://HOST/DBNAME?sslmode=require`)
- `DB_USER`
- `DB_PASSWORD`

## Roadmap
- JWT authentication with customer and admin roles
- Unit and integration tests