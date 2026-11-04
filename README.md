# Banking System (Spring Boot REST API)
**Live demo:** https://banking-system-r1q8.onrender.com (free hosting, the first request may take about a minute to wake up)
**API docs (Swagger UI):** https://banking-system-r1q8.onrender.com/swagger-ui/index.html
A simple banking REST API built with Java and Spring Boot. It supports creating accounts, deposits, withdrawals, money transfers and transaction history.

## Tech Stack
- Java 17
- Spring Boot, Spring Data JPA (Hibernate)
- H2 in-memory database
- Maven

## Features
- Create a bank account with an initial deposit
- Deposit and withdraw money
- Transfer money between accounts (atomic, rolls back on failure using `@Transactional`)
- Optimistic locking to handle concurrent updates
- Input validation and global exception handling
- Transaction history for every account

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
1. Install JDK 17 or higher
2. Clone the repository:
   ```
   git clone https://github.com/etteyjayadeep2005-bit/banking-system.git
   ```
3. Go into the folder and run:
   ```
   ./mvnw spring-boot:run
   ```
4. The API starts at `http://localhost:8080`

## Roadmap
- PostgreSQL database
- JWT authentication with customer and admin roles
- Unit and integration tests
- Swagger API documentation