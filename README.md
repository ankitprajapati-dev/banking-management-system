# 🏦 BankGuard - Banking Management System

## 📖 Overview

**BankGuard** is a secure **RESTful Banking Management System** built using **Spring Boot**, **Spring Security**, **JWT authentication**, **Spring Data JPA**, and **PostgreSQL**.

The application provides core banking functionality such as user registration and authentication, account management, beneficiary management, deposits, withdrawals, fund transfers, transaction history, and role-based administrative operations.

The project follows a layered architecture with separate **Controller, Service, Repository, DTO, Entity, Exception, and Security** layers, making the application easier to maintain, test, and extend.

---

## ✨ Features

### 👤 User Management

* User registration and authentication
* JWT-based authentication
* BCrypt password hashing
* Role-based access control
* Customer and Admin roles
* Secure logout using JWT token blacklisting

### 💳 Account Management

* Create bank accounts
* Support for **SAVINGS** and **CURRENT** account types
* View account details
* View all customer accounts
* Calculate total account balance
* Maximum of 5 accounts per customer
* Account status management: **ACTIVE, BLOCKED, CLOSED**
* Account ownership validation

### 💰 Transaction Management

* Deposit money
* Withdraw money
* Transfer funds between accounts
* Transaction reference generation and tracking
* Transaction history
* Date-range transaction filtering
* Mini statement showing the latest 10 transactions
* Transaction ownership validation
* Daily outgoing transfer limit of **₹1,00,000**
* Minimum balance validation of **₹500 for SAVINGS accounts**
* Validation for active source and destination accounts

### 👥 Beneficiary Management

* Add beneficiaries
* View active beneficiaries
* Update beneficiary details
* Soft delete/block beneficiaries
* IFSC code validation
* Duplicate beneficiary prevention
* Customer ownership validation

### 🔐 Security Features

* JWT-based authentication
* 15-minute JWT expiration
* BCrypt password hashing
* Role-based authorization
* Customer/Admin access separation
* JWT token blacklisting on logout
* Stateless session management
* Account status validation
* Ownership-based access validation
* Centralized exception handling

### 📚 API Documentation

* Swagger / OpenAPI integration
* Interactive API documentation
* API testing through Swagger UI

---

## 🛠️ Technology Stack

| Category           | Technology                  | Version     |
| ------------------ | --------------------------- | ----------- |
| **Language**       | Java                        | 21          |
| **Framework**      | Spring Boot                 | 3.3.4       |
| **Security**       | Spring Security             | 6.x         |
| **Authentication** | JWT                         | JJWT 0.12.6 |
| **Database**       | PostgreSQL                  | 15+         |
| **ORM**            | Spring Data JPA + Hibernate | 6.x         |
| **API**            | RESTful API + OpenAPI       | 3.0         |
| **Build Tool**     | Maven                       | 3.9+        |
| **Validation**     | Jakarta Bean Validation     | -           |
| **Utilities**      | Lombok, SLF4J               | -           |

---

## 📁 Project Structure

```text
banking-management-system/
│
├── src/
│   ├── main/
│   │   ├── java/com/bankguard/banking/
│   │   │
│   │   ├── config/          # Application and security configuration
│   │   ├── controller/      # REST API controllers
│   │   ├── dto/
│   │   │   ├── request/     # Request DTOs
│   │   │   └── response/    # Response DTOs
│   │   ├── entity/          # JPA entities and enums
│   │   ├── exception/       # Custom exceptions and global handler
│   │   ├── repository/      # Spring Data JPA repositories
│   │   ├── security/        # JWT and authentication components
│   │   └── service/         # Business logic
│   │
│   └── resources/
│       └── application.properties
│
├── src/test/
│   └── resources/
│       └── application.properties
│
├── pom.xml                  # Maven configuration
├── README.md
└── LICENSE
```

---

## 🚀 Getting Started

### Prerequisites

Make sure the following are installed:

* Java 21 or higher
* Maven 3.9 or higher
* PostgreSQL 15 or higher
* Eclipse / Spring Tool Suite (optional)
* Postman or another REST API client

---

### 1. Clone the Repository

```bash
git clone https://github.com/ankitprajapati-dev/banking-management-system.git

cd banking-management-system
```

---

### 2. Create the PostgreSQL Database

Open PostgreSQL and create the database:

```sql
CREATE DATABASE banking_management;
```

The application will connect to:

```text
jdbc:postgresql://localhost:5432/banking_management
```

---

### 3. Configure Environment Variables

The application keeps sensitive configuration outside the source code.

#### Required Environment Variables

```text
DB_PASSWORD=your_postgresql_password
JWT_SECRET=your_base64_secret_key
ADMIN_PASSWORD=your_admin_password
```

The application also supports an optional admin username:

```text
ADMIN_USERNAME=admin
```

If `ADMIN_USERNAME` is not provided, the default value is:

```text
admin
```

### Important

Do **not** commit real passwords, JWT secrets, or database credentials to GitHub.

---

### 4. Build the Project

```bash
mvn clean verify
```

A successful build should show:

```text
BUILD SUCCESS
```

---

### 5. Run the Application

Using Maven:

```bash
mvn spring-boot:run
```

Or run:

```text
BankingManagementSystemApplication.java
```

from Eclipse / Spring Tool Suite using:

```text
Run As → Spring Boot App
```

The application runs on:

```text
http://localhost:8080/api
```

---

# 🔗 API Endpoints

> All protected endpoints require a valid JWT Bearer token unless explicitly marked as public.

## 🔐 Authentication Endpoints

### Register User

**POST**

```text
/api/auth/register
```

#### Request Body

```json
{
  "username": "john_doe",
  "password": "SecurePass123",
  "fullName": "John Doe",
  "email": "john@example.com",
  "phone": "9876543210"
}
```

### Login User

**POST**

```text
/api/auth/login
```

#### Request Body

```json
{
  "username": "john_doe",
  "password": "SecurePass123"
}
```

#### Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "username": "john_doe",
  "role": "CUSTOMER"
}
```

### Logout

**POST**

```text
/api/auth/logout
```

#### Header

```text
Authorization: Bearer <token>
```

---

# 💳 Account Endpoints

| Method | Endpoint                      | Description               | Authentication |
| ------ | ----------------------------- | ------------------------- | -------------- |
| POST   | `/api/accounts`               | Create a new account      | ✅ Bearer Token |
| GET    | `/api/accounts`               | Get all customer accounts | ✅ Bearer Token |
| GET    | `/api/accounts/{id}`          | Get account by ID         | ✅ Bearer Token |
| GET    | `/api/accounts/balance/total` | Get total balance         | ✅ Bearer Token |

### Create Account

**POST**

```text
/api/accounts
```

#### Request Body

```json
{
  "accountType": "SAVINGS"
}
```

Supported account types:

```text
SAVINGS
CURRENT
```

A newly created account starts with a zero balance. Funds can then be added using the deposit endpoint.

---

# 💰 Transaction Endpoints

| Method | Endpoint                           | Description                | Authentication |
| ------ | ---------------------------------- | -------------------------- | -------------- |
| POST   | `/api/transactions/deposit`        | Deposit money              | ✅ Bearer Token |
| POST   | `/api/transactions/withdraw`       | Withdraw money             | ✅ Bearer Token |
| POST   | `/api/transactions/transfer`       | Transfer funds             | ✅ Bearer Token |
| GET    | `/api/transactions`                | Get transaction history    | ✅ Bearer Token |
| GET    | `/api/transactions/mini-statement` | Get latest 10 transactions | ✅ Bearer Token |
| GET    | `/api/transactions/{id}`           | Get transaction by ID      | ✅ Bearer Token |

### Deposit

**POST**

```text
/api/transactions/deposit
```

#### Request Body

```json
{
  "accountId": 1,
  "amount": 5000.00,
  "description": "Salary deposit"
}
```

### Withdraw

**POST**

```text
/api/transactions/withdraw
```

#### Request Body

```json
{
  "accountId": 1,
  "amount": 1000.00,
  "description": "ATM withdrawal"
}
```

### Transfer

**POST**

```text
/api/transactions/transfer
```

#### Request Body

```json
{
  "accountId": 1,
  "destinationAccountId": 2,
  "amount": 1000.00,
  "description": "Transfer to friend"
}
```

### Transaction History

**GET**

```text
/api/transactions
```

Supports transaction history retrieval and date-range filtering according to the API implementation.

### Mini Statement

**GET**

```text
/api/transactions/mini-statement
```

Returns the latest **10 transactions** for the authenticated customer.

---

# 👥 Beneficiary Endpoints

| Method | Endpoint                  | Description              | Authentication |
| ------ | ------------------------- | ------------------------ | -------------- |
| POST   | `/api/beneficiaries`      | Add beneficiary          | ✅ Bearer Token |
| GET    | `/api/beneficiaries`      | Get active beneficiaries | ✅ Bearer Token |
| PUT    | `/api/beneficiaries/{id}` | Update beneficiary       | ✅ Bearer Token |
| DELETE | `/api/beneficiaries/{id}` | Block beneficiary        | ✅ Bearer Token |

### Add Beneficiary

**POST**

```text
/api/beneficiaries
```

#### Request Body

```json
{
  "nickname": "Rahul's Account",
  "beneficiaryAccountNumber": "123456789012",
  "bankName": "State Bank of India",
  "ifscCode": "SBIN0001234"
}
```

The beneficiary delete operation is implemented as a **soft delete/block**, so the beneficiary record is retained in the database.

---

# 👑 Admin Endpoints

Admin endpoints require the **ADMIN** role.

| Method | Endpoint                           | Description      | Authentication |
| ------ | ---------------------------------- | ---------------- | -------------- |
| GET    | `/api/admin/accounts`              | Get all accounts | ✅ ADMIN        |
| PUT    | `/api/admin/accounts/{id}/block`   | Block account    | ✅ ADMIN        |
| PUT    | `/api/admin/accounts/{id}/unblock` | Unblock account  | ✅ ADMIN        |

Customers attempting to access admin endpoints receive:

```text
403 Forbidden
```

---

# 🏥 Health Check

**GET**

```text
/api/health
```

Authentication:

```text
Public
```

Example response:

```json
{
  "status": "UP"
}
```

---

# 🗄️ Database Configuration

The application uses **PostgreSQL** as its database.

### Database

```text
Database Name: banking_management
Username: postgres
Port: 5432
```

### Main Configuration

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/banking_management
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
```

The PostgreSQL password is provided through the `DB_PASSWORD` environment variable.

---

# 🔐 Security Configuration

| Security Component | Configuration             |
| ------------------ | ------------------------- |
| Authentication     | JWT                       |
| Token Type         | Bearer                    |
| JWT Expiration     | 15 minutes                |
| Algorithm          | HS256                     |
| Password Hashing   | BCrypt                    |
| Authorization      | Role-Based Access Control |
| Session Management | Stateless                 |
| Logout             | JWT Token Blacklisting    |

### Access Control

| Endpoint                  | Access              |
| ------------------------- | ------------------- |
| `/api/auth/**`            | Public              |
| `/api/health`             | Public              |
| `/api/admin/**`           | ADMIN only          |
| Other `/api/**` endpoints | Authenticated users |

---

## 👤 Roles

### CUSTOMER

Customers can:

* Manage their own accounts
* View their accounts
* Perform deposits and withdrawals
* Transfer funds
* Manage beneficiaries
* View their transaction history

### ADMIN

Administrators can:

* View all accounts
* Block accounts
* Unblock accounts

---

# 📊 Business Rules

The application implements the following core banking validations:

### Account Limit

A customer can create a maximum of:

```text
5 accounts
```

### Minimum Savings Balance

For SAVINGS accounts:

```text
₹500 minimum balance
```

### Daily Transfer Limit

Maximum outgoing transfer amount per day:

```text
₹1,00,000
```

### Account Status

Transactions are allowed only for:

```text
ACTIVE
```

accounts.

Blocked or closed accounts cannot perform normal banking operations.

### Transaction Validation

The application validates:

* Account ownership
* Account existence
* Account status
* Positive transaction amount
* Sufficient balance
* Source and destination account validity
* Same-account transfer prevention
* Daily transfer limit
* Savings minimum balance

---

# 🧪 Testing

The project was tested using:

* Maven test execution
* Eclipse / Spring Tool Suite
* Postman
* PostgreSQL database

### Maven Test

Run:

```bash
mvn clean verify
```

Expected result:

```text
Tests run: 1
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

### API Testing with Postman

Recommended flow:

```text
1. Register a customer
2. Login and obtain JWT token
3. Create an account
4. Deposit funds
5. Withdraw funds
6. Create another account
7. Transfer funds
8. Add beneficiary
9. View transaction history
10. Test admin authorization
```

Security scenarios should also be tested:

```text
No token → 401 Unauthorized
Customer → Admin endpoint → 403 Forbidden
Invalid resource → 404 Not Found
```

---

# 📚 API Documentation

Swagger UI is available through the application's OpenAPI configuration.

Typical endpoint:

```text
/api/swagger-ui/index.html
```

OpenAPI specification:

```text
/api/v3/api-docs
```

---

# ⚠️ Known Limitations

The current version intentionally focuses on core banking functionality.

The following features are not currently implemented:

* Email transaction notifications
* SMS notifications
* Two-factor authentication
* PDF bank statement generation
* Advanced dashboard analytics
* Automated interest calculation
* Full audit logging for administrative actions
* Production-grade distributed token/session management

---

# 🚀 Future Enhancements

Potential future improvements include:

* Email and SMS notifications
* Two-factor authentication
* PDF statement generation
* Banking dashboard and analytics
* Automated savings interest calculation
* Advanced audit logging
* Improved test coverage
* Production-ready deployment configuration
* Containerization using Docker

---

# 🔧 Troubleshooting

| Issue                        | Solution                                                     |
| ---------------------------- | ------------------------------------------------------------ |
| Port 8080 already in use     | Change `server.port` in `application.properties`             |
| PostgreSQL connection failed | Make sure PostgreSQL is running and `DB_PASSWORD` is correct |
| Database does not exist      | Create `banking_management` database                         |
| JWT startup/validation error | Verify `JWT_SECRET` is configured correctly                  |
| Admin initialization error   | Verify `ADMIN_PASSWORD` is configured                        |
| 401 Unauthorized             | Login again and provide a valid Bearer token                 |
| 403 Forbidden                | Verify that the authenticated user has the required role     |

---

# 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test the application
5. Commit your changes
6. Push the branch
7. Create a Pull Request

---

# 📄 License

This project is licensed under the **MIT License**.

---

# 📬 Contact

**Ankit Prajapati**

* Email: [aankit1645@gmail.com](mailto:aankit1645@gmail.com)
* GitHub: [ankitprajapati-dev](https://github.com/ankitprajapati-dev)

---

⭐ **If you found this project useful, consider giving the repository a star!** ⭐

---

**Last Updated:** September 2026

**Version:** 1.0.0
