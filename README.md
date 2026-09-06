# 🏦 Online Banking System - Spring Boot Application

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/)
[![JWT](https://img.shields.io/badge/JWT-Authentication-red.svg)](https://jwt.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## 📖 Overview

A comprehensive **RESTful online banking application** built with **Spring Boot**, featuring secure user authentication, account management, and financial transactions. This project demonstrates clean architecture, enterprise-level security, and best practices in Spring Boot development.

---

## ✨ Features

### 👤 User Management
- User registration and authentication
- JWT-based token security
- Role-based access control (RBAC)
- BCrypt password encryption

### 💳 Account Management
- Create multiple account types (**SAVINGS**, **CURRENT**)
- View account details and balance
- Activate/Deactivate accounts
- Multiple accounts per customer (max 5)

### 💰 Transaction Management
- Fund transfers between accounts
- Deposit and withdrawal operations
- Transaction history with pagination
- Date range filtering for transactions
- Transaction reference tracking
- Mini statement (last 10 transactions)

### 👥 Beneficiary Management
- Add external account beneficiaries
- Update beneficiary details
- Soft delete (block) beneficiaries
- IFSC code validation
- Duplicate beneficiary prevention

### 🔐 Security Features
- JWT authentication with 15-minute expiry
- BCrypt password encryption
- Role-based access (**CUSTOMER**, **ADMIN**)
- Token blacklisting for logout
- Account status validation (ACTIVE, BLOCKED, CLOSED)
- Daily transaction limit (₹1,00,000)
- Minimum balance validation (₹500 for savings)

### 📚 API Documentation
- Swagger/OpenAPI 3.0 integration
- Interactive API testing via Swagger UI

---

## 🛠️ Technology Stack

| Category | Technology | Version |
|----------|------------|---------|
| **Framework** | Spring Boot | 3.3.4 |
| **Language** | Java | 21 |
| **Database** | PostgreSQL | 15+ |
| **Security** | Spring Security + JWT | JJWT 0.12.6 |
| **ORM** | Spring Data JPA + Hibernate | - |
| **API** | RESTful API with OpenAPI | - |
| **Build Tool** | Maven | 3.9+ |
| **Utilities** | Lombok, Jakarta Validation, SLF4J | - |

---

## 📁 Project Structure

```
banking-management-system/
│
├── src/main/java/com/bankguard/banking/
│   ├── config/          # Configuration classes (Security, Web, Async)
│   ├── controller/      # REST API controllers
│   ├── dto/             # Data Transfer Objects (Request/Response)
│   ├── entity/          # JPA entities
│   ├── exception/       # Exception handling
│   ├── repository/      # Data access layer
│   ├── security/        # JWT and security utilities
│   └── service/         # Business logic
│
├── src/main/resources/
│   └── application.properties  # Configuration properties
│
├── pom.xml              # Maven dependencies
└── README.md            # This file
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.9.0 or higher
- PostgreSQL 15+ (for production)
- Postman or cURL (for API testing)

### Installation

**1. Clone the repository**
```bash
git clone https://github.com/YOUR_USERNAME/banking-management-system.git
cd banking-management-system
```

**2. Setup PostgreSQL database**
```bash
psql -U postgres
CREATE DATABASE banking_management;
\q
```

**3. Configure environment variables**
Create `.env` file in project root:
```properties
DB_PASSWORD=your_password
JWT_SECRET=your_32_character_secret_key
```

**4. Build the project**
```bash
mvn clean install
```

**5. Run the application**
```bash
mvn spring-boot:run
```

**Application starts on:** `http://localhost:8080/api`

---

## 🔗 API Endpoints

### 🔐 Authentication Endpoints

#### Register User
**POST** `/api/auth/register`

**Request body:**
```json
{
  "username": "john_doe",
  "password": "SecurePass123",
  "fullName": "John Doe",
  "email": "john@example.com",
  "phone": "9876543210",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY",
  "pincode": "10001",
  "panNumber": "ABCDE1234F",
  "acceptTerms": true
}
```

#### Login User
**POST** `/api/auth/login`

**Request body:**
```json
{
  "username": "john_doe",
  "password": "SecurePass123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "username": "john_doe",
  "role": "CUSTOMER"
}
```

#### Logout
**POST** `/api/auth/logout`  
**Headers:** `Authorization: Bearer <token>`

---

### 💳 Account Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/accounts` | Create new account | ✅ Bearer Token |
| GET | `/api/accounts` | Get all user accounts | ✅ Bearer Token |
| GET | `/api/accounts/{id}` | Get account by ID | ✅ Bearer Token |
| GET | `/api/accounts/balance/total` | Get total balance | ✅ Bearer Token |

**Create Account Request Body:**
```json
{
  "accountType": "SAVINGS",
  "initialBalance": 1000.00
}
```

---

### 💰 Transaction Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/transactions/deposit` | Deposit money | ✅ Bearer Token |
| POST | `/api/transactions/withdraw` | Withdraw money | ✅ Bearer Token |
| POST | `/api/transactions/transfer` | Transfer funds | ✅ Bearer Token |
| GET | `/api/transactions` | Get all transactions | ✅ Bearer Token |
| GET | `/api/transactions/mini-statement` | Last 10 transactions | ✅ Bearer Token |
| GET | `/api/transactions/{id}` | Get transaction by ID | ✅ Bearer Token |

**Deposit/Withdraw Request Body:**
```json
{
  "accountId": 1,
  "amount": 5000.00,
  "description": "Salary deposit"
}
```

**Transfer Request Body:**
```json
{
  "accountId": 1,
  "destinationAccountId": 2,
  "amount": 1000.00,
  "description": "Transfer to friend"
}
```

---

### 👥 Beneficiary Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/beneficiaries` | Add beneficiary | ✅ Bearer Token |
| GET | `/api/beneficiaries` | Get all beneficiaries | ✅ Bearer Token |
| PUT | `/api/beneficiaries/{id}` | Update beneficiary | ✅ Bearer Token |
| DELETE | `/api/beneficiaries/{id}` | Delete beneficiary | ✅ Bearer Token |

**Add Beneficiary Request Body:**
```json
{
  "nickname": "Rahul's Account",
  "beneficiaryAccountNumber": "123456789012",
  "bankName": "State Bank of India",
  "ifscCode": "SBIN0001234"
}
```

---

### 👑 Admin Endpoints (ADMIN Role Required)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/admin/accounts` | Get all accounts | ✅ Bearer Token |
| PUT | `/api/admin/accounts/{id}/block` | Block account | ✅ Bearer Token |
| PUT | `/api/admin/accounts/{id}/unblock` | Unblock account | ✅ Bearer Token |

---

### 🏥 Health Check

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/health` | Application health | ❌ Public |

**Response:**
```json
{
  "status": "UP"
}
```

---

## 🗄️ Database Configuration

### For Development (H2 Database)
```properties
spring.datasource.url=jdbc:h2:mem:banking_db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### For Production (PostgreSQL)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/banking_management
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

---

## 🔐 Security Configuration

| Component | Configuration |
|-----------|---------------|
| **Token Type** | JWT (JSON Web Token) |
| **Secret Key** | `JWT_SECRET` environment variable |
| **Expiration** | 900000 ms (15 minutes) |
| **Algorithm** | HS256 |
| **Password Encoding** | BCrypt |
| **Min Password Length** | 8 characters |

### Access Control
| Endpoint Type | Access |
|---------------|--------|
| `/api/auth/**`, `/api/health` | Public |
| `/api/**` (except auth) | Authenticated |
| `/api/admin/**` | ADMIN role required |

### Default Credentials
| Role | Username | Password |
|------|----------|----------|
| **ADMIN** | admin | Admin@123 |
| **CUSTOMER** | customer | Customer@123 |

---

## 📊 Project Features Summary

| Feature | Status |
|---------|--------|
| JWT Authentication | ✅ |
| Role-Based Access | ✅ |
| Account Management | ✅ |
| Transactions (Deposit/Withdraw/Transfer) | ✅ |
| Beneficiary Management | ✅ |
| Daily Transaction Limit | ✅ |
| Minimum Balance Validation | ✅ |
| Swagger Documentation | ✅ |
| Global Exception Handling | ✅ |
| Pagination Support | ✅ |

---

## ⚠️ Known Limitations

- No email notifications for transactions
- No SMS alerts
- No interest calculation for savings accounts
- No overdraft protection
- No audit logging for admin actions

---

## 🚀 Planned Enhancements

- Email notifications for transactions
- SMS alerts
- 2FA (Two-Factor Authentication)
- PDF statement generation
- Dashboard analytics
- Scheduled interest calculation
- Unit and integration tests

---

## 🧪 Testing

### Using Postman
1. Import API endpoints
2. Register user and get JWT token
3. Add token to Authorization header
4. Test endpoints

### Using cURL
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username":"john_doe",
    "password":"SecurePass123",
    "fullName":"John Doe",
    "email":"john@example.com",
    "phone":"9876543210",
    "address":"123 Main St",
    "city":"New York",
    "state":"NY",
    "pincode":"10001",
    "panNumber":"ABCDE1234F",
    "acceptTerms":true
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username":"john_doe",
    "password":"SecurePass123"
  }'

# Create Account
curl -X POST http://localhost:8080/api/accounts \
  -H "Authorization: Bearer <your_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "accountType":"SAVINGS",
    "initialBalance":1000.00
  }'
```

---

## 🔧 Troubleshooting

| Issue | Solution |
|-------|----------|
| Port 8080 already in use | Change `server.port=8081` in `application.properties` |
| Database connection error | Ensure PostgreSQL is running: `net start postgresql` |
| JWT token validation failure | Ensure `JWT_SECRET` is set and at least 32 characters |

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

---

## 📄 License

This project is licensed under the **MIT License**.

---

## 📬 Contact

**Your Name**
- Email: aankit1645@gmail.com
- GitHub: [github.com/ankitprajapati-dev](https://github.com/ankitprajapati-dev)

---

⭐ **If you found this project helpful, please give it a star!** ⭐

---

**Last Updated:** September 2026  
**Version:** 1.0.0