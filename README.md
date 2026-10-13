# Smart Pharmacy — Backend

A production-style REST API for pharmacy management built with **Spring Boot 4** and **MySQL 8**. Features JWT authentication, role-based access control, transactional point-of-sale processing, inventory management, and analytics.

![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Tests](https://img.shields.io/badge/Tests-31%20passing-success)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Tech Stack](#-tech-stack)
- [Features](#-features)
- [Architecture](#-architecture)
- [Data Model](#-data-model)
- [API Endpoints](#-api-endpoints)
- [Getting Started](#-getting-started)
- [Running Tests](#-running-tests)
- [Project Structure](#-project-structure)
- [Roadmap](#-roadmap)
- [Author](#-author)

---

## 📖 Overview

Smart Pharmacy is a backend REST API that simulates a real-world pharmacy management system. It provides:

- A secure REST API for pharmacy operations
- JWT-based authentication with 4 user roles
- Transactional point-of-sale processing with stock management
- Full inventory management with soft-delete and stock alerts
- Automated tests at unit, controller, and integration levels

The primary goal was to demonstrate **production-grade Java backend skills**: layered architecture, Spring Security, JPA relationships, DTOs, exception handling, and testing.

> **Note:** A companion frontend (vanilla HTML/CSS/JS) is in progress and will be added as a separate repository.

---

## 🚀 Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 25 | Language |
| Spring Boot | 4.1.1 | Application framework |
| Spring Security | 7.1.1 | Authentication & authorization |
| Spring Data JPA | 4.1.1 | Data access |
| Hibernate | 7.4.5 | ORM |
| MySQL | 8.0.46 | Production database |
| H2 | 2.4.240 | Dev & test database |
| Gradle | 9.x | Build tool (Groovy DSL) |
| Lombok | 1.18.42 | Boilerplate reduction |
| JJWT | 0.12.6 | JWT library |
| SpringDoc OpenAPI | 2.7.0 | Swagger UI |
| JUnit 5 | 5.x | Testing |
| Mockito | 5.x | Mocking |

---

## ✨ Features

### 🔐 Authentication & Authorization
- **JWT**-based stateless authentication
- **BCrypt** password hashing
- **Role-based access control** with 4 roles:
    - `ADMIN` — full access
    - `PHARMACIST` — create/update medicines, sales, refunds
    - `CASHIER` — create sales only
    - `CUSTOMER` — read-only
- Method-level security via `@PreAuthorize`

### 💊 Inventory Management
- Full CRUD for **medicines**, **categories**, **suppliers**, **customers**
- **Barcode** lookup
- **Low-stock alerts** — medicines where `quantity <= reorderLevel`
- **Expiry tracking** — medicines expiring within N days
- **Soft delete** — medicines marked `active=false`, never hard-deleted

### 🛒 Point of Sale (POS)
- Cart with quantity controls
- **Atomic stock decrement** (transactional)
- **5% tax** calculation
- **Discount** support
- **Payment methods**: Cash / Card / Mobile
- **Refund** support — restores stock and marks sale as `REFUNDED`
- **Loyalty points** — customers earn 1 point per 10 currency units spent

### 📊 Dashboard Analytics
- Total medicines, customers, suppliers
- Low-stock count
- Expiring-soon count
- Today's revenue
- Monthly revenue
- Today's sales count

### 🛡️ Engineering Quality
- **Global exception handling** with `@RestControllerAdvice`
- **Validation** with Jakarta Bean Validation
- **DTO layer** — entities never leak to API
- **Consistent responses** via `ApiResponse<T>` wrapper
- **31 automated tests** across 3 levels

---

## 🏗️ Architecture

### Layered Design

```
┌──────────────────────────────────────────────────────────┐
│                    HTTP Request                          │
└─────────────────────────┬────────────────────────────────┘
                          ▼
┌──────────────────────────────────────────────────────────┐
│  JwtAuthenticationFilter — validates JWT, sets context   │
└─────────────────────────┬────────────────────────────────┘
                          ▼
┌──────────────────────────────────────────────────────────┐
│  @RestController — HTTP concerns, @PreAuthorize checks   │
└─────────────────────────┬────────────────────────────────┘
                          ▼
┌──────────────────────────────────────────────────────────┐
│  @Service — business logic, @Transactional boundaries    │
└─────────────────────────┬────────────────────────────────┘
                          ▼
┌──────────────────────────────────────────────────────────┐
│  @Repository (Spring Data JPA) — data access             │
└─────────────────────────┬────────────────────────────────┘
                          ▼
┌──────────────────────────────────────────────────────────┐
│  Hibernate ORM ──▶ MySQL / H2                            │
└──────────────────────────────────────────────────────────┘
```

### Package Structure

```
controller/     → REST endpoints
service/        → business logic
repository/     → Spring Data JPA interfaces
entity/         → JPA entities
dto/            → request/response contracts
security/       → JWT filter, service, user details
exception/      → global handler + custom exceptions
config/         → SecurityConfig, OpenApiConfig, DataInitializer
```

---

## 📦 Data Model

### Entity Relationships

```
User ──(cashier)──▶ Sale ◀──(customer)── Customer
                     │
                     │ 1..N
                     ▼
                  SaleItem
                     │
                     │ N..1
                     ▼
                  Medicine ──▶ Category
                     │
                     └─────▶ Supplier
```

**9 tables:** `users`, `medicines`, `categories`, `suppliers`, `customers`, `sales`, `sale_items`, `prescriptions`, `stock_alerts`

### Key Entities

| Entity | Purpose |
|---|---|
| `User` | Application users with roles |
| `Medicine` | Inventory items with stock, price, expiry |
| `Category` | Medicine classification |
| `Supplier` | Vendors who supply medicines |
| `Customer` | Buyers (optional — walk-in allowed) |
| `Sale` | Completed transactions with invoice |
| `SaleItem` | Line items in a sale |
| `Prescription` | Customer prescriptions (scaffolded) |
| `StockAlert` | Low-stock / expiry notifications (scaffolded) |

---

## 🔌 API Endpoints

**Base URL:** `http://localhost:8080/api`

### Authentication (public)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/auth/register` | Register a new user |
| `POST` | `/auth/login` | Login and receive JWT |

### Medicines

| Method | Endpoint | Role |
|---|---|---|
| `GET` | `/medicines` | Any |
| `GET` | `/medicines/{id}` | Any |
| `GET` | `/medicines/search?keyword=x` | Any |
| `GET` | `/medicines/low-stock` | Any |
| `GET` | `/medicines/expiring?days=30` | Any |
| `POST` | `/medicines` | ADMIN, PHARMACIST |
| `PUT` | `/medicines/{id}` | ADMIN, PHARMACIST |
| `DELETE` | `/medicines/{id}` | ADMIN |

### Categories

| Method | Endpoint | Role |
|---|---|---|
| `GET` | `/categories` | Any |
| `POST` | `/categories` | ADMIN, PHARMACIST |
| `PUT` | `/categories/{id}` | ADMIN, PHARMACIST |
| `DELETE` | `/categories/{id}` | ADMIN |

### Suppliers

| Method | Endpoint | Role |
|---|---|---|
| `GET` | `/suppliers` | Any |
| `POST` | `/suppliers` | ADMIN, PHARMACIST |
| `PUT` | `/suppliers/{id}` | ADMIN, PHARMACIST |
| `DELETE` | `/suppliers/{id}` | ADMIN |

### Customers

| Method | Endpoint | Role |
|---|---|---|
| `GET` | `/customers` | Any |
| `POST` | `/customers` | Any |
| `PUT` | `/customers/{id}` | Any |
| `DELETE` | `/customers/{id}` | Any |

### Sales

| Method | Endpoint | Role |
|---|---|---|
| `GET` | `/sales` | Any |
| `GET` | `/sales/{id}` | Any |
| `GET` | `/sales/today` | Any |
| `POST` | `/sales` | ADMIN, PHARMACIST, CASHIER |
| `POST` | `/sales/{id}/refund` | ADMIN, PHARMACIST |

### Dashboard

| Method | Endpoint | Role |
|---|---|---|
| `GET` | `/dashboard/stats` | Any |

**Full API docs:** http://localhost:8080/api/swagger-ui.html

---

## 🚦 Getting Started

### Prerequisites

- **Java 25** — [Adoptium JDK 25](https://adoptium.net/)
- **Git**
- **MySQL 8** (optional — H2 works out of the box)

### 1. Clone

```bash
git clone https://github.com/rsrabbey17/Smart-Pharmacy-Management.git
cd Smart-Pharmacy-Management
```

### 2. Choose a database

#### Option A — H2 in-memory (quickest, no install)

Edit `src/main/resources/application.yml`:

```yaml
spring:
  profiles:
    active: dev
```

Skip to step 3.

#### Option B — MySQL (persistent)

Create the database and user:

```sql
CREATE DATABASE smart_pharmacy
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER 'pharmacy_user'@'localhost'
    IDENTIFIED BY 'pharmacy_pass';

GRANT ALL PRIVILEGES ON smart_pharmacy.* TO 'pharmacy_user'@'localhost';
FLUSH PRIVILEGES;
```

Edit `src/main/resources/application.yml`:

```yaml
spring:
  profiles:
    active: mysql
```

If your MySQL password is different, update `application-mysql.yml`.

### 3. Run

```bash
./gradlew bootRun
```

The server starts on **http://localhost:8080/api**

### 4. Log in

| Field | Value |
|---|---|
| Username | `admin` |
| Password | `admin123` |

### 5. Access points

| URL | Purpose |
|---|---|
| http://localhost:8080/api/swagger-ui.html | Interactive API docs |
| http://localhost:8080/api/actuator/health | Health check |
| http://localhost:8080/api/h2-console | H2 console (dev only) |

---

## 🧪 Running Tests

```bash
./gradlew test
```

**Expected:** `31 tests completed, 0 failed`

### Test Breakdown

| Level | Count | Framework | Scope |
|---|---|---|---|
| Service (unit) | 23 | JUnit 5 + Mockito | Business logic in isolation |
| Controller (`@WebMvcTest`) | 4 | MockMvc | REST endpoints, JSON, status codes |
| Integration (`@SpringBootTest`) | 3 | SpringBootTest + H2 | Full HTTP → DB → JSON flow |

### Test Classes

```
src/test/java/com/pharmacy/smart_pharmacy/
├── service/
│   ├── AuthServiceTest.java           (7 tests)
│   ├── MedicineServiceTest.java       (9 tests)
│   └── SaleServiceTest.java           (7 tests)
├── controller/
│   └── AuthControllerTest.java        (4 tests)
└── integration/
    └── AuthIntegrationTest.java       (3 tests)
```

**Run a single test class:**

```bash
./gradlew test --tests "com.pharmacy.smart_pharmacy.service.SaleServiceTest"
```

---

## 📁 Project Structure

```
.
├── src/
│   ├── main/
│   │   ├── java/com/pharmacy/smart_pharmacy/
│   │   │   ├── SmartPharmacyApplication.java
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── MedicineController.java
│   │   │   │   ├── SaleController.java
│   │   │   │   └── SupplierController.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── entity/
│   │   │   │   ├── Category.java
│   │   │   │   ├── Customer.java
│   │   │   │   ├── Medicine.java
│   │   │   │   ├── Prescription.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── Sale.java
│   │   │   │   ├── SaleItem.java
│   │   │   │   ├── StockAlert.java
│   │   │   │   ├── Supplier.java
│   │   │   │   └── User.java
│   │   │   ├── exception/
│   │   │   │   ├── BadRequestException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtService.java
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-mysql.yml
│   └── test/
│       ├── java/com/pharmacy/smart_pharmacy/
│       │   ├── service/
│       │   ├── controller/
│       │   └── integration/
│       └── resources/
│           └── application-test.yml
├── .gitignore
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/
└── README.md
```

---

## 🗺️ Roadmap

### ✅ Completed
- [x] JWT authentication with BCrypt hashing
- [x] Role-based access control (4 roles)
- [x] Medicine, category, supplier, customer CRUD
- [x] Point of Sale with tax, discount, refunds
- [x] Loyalty points system
- [x] Low-stock and expiry alerts
- [x] Dashboard analytics
- [x] MySQL persistence
- [x] 31 unit + integration tests
- [x] Swagger/OpenAPI documentation
- [x] Global exception handling
- [x] Soft delete for medicines

### 🚧 Planned
- [ ] Dockerfile + docker-compose
- [ ] GitHub Actions CI pipeline
- [ ] Frontend (HTML/CSS/JS) — separate repo
- [ ] Deployment to Render / Railway
- [ ] Prescription workflow (upload, approve, dispense)
- [ ] Email notifications for low stock
- [ ] PDF invoice generation
- [ ] Report export (CSV / Excel)

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Rabbey**
- GitHub: [@rsrabbey17](https://github.com/rsrabbey17)
- Repository: [Smart-Pharmacy-Management](https://github.com/rsrabbey17/Smart-Pharmacy-Management)

---

## 🙏 Acknowledgments

- [Spring Boot](https://docs.spring.io/spring-boot/)
- [Hibernate ORM](https://hibernate.org/orm/documentation/)
- [JWT.io](https://jwt.io/)
- [Baeldung](https://www.baeldung.com/)