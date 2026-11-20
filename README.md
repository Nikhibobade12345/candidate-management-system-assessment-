# Candidate Management System

A production-ready RESTful API built with **Java 17** and **Spring Boot** to manage, search, and filter candidate profiles for recruitment workflows.

---

## 🚀 Key Features

- **Candidate CRUD**: Full lifecycle management (Create, Read, Update, Delete) with comprehensive input validation.
- **Dynamic Keyword Search**: Search candidates across `firstName`, `lastName`, `email`, and `currentCompany` using case-insensitive partial matching.
- **Multi-Criteria Filtering**: Filter candidates dynamically using JPA Specifications by:
  - Candidate status (`AVAILABLE`, `IN_PROGRESS`, `HIRED`, `REJECTED`, `ON_HOLD`)
  - Location (case-insensitive partial match)
  - Experience range (`minExperience` & `maxExperience`)
  - Expected salary range (`minSalary` & `maxSalary`)
  - Maximum notice period in days (`maxNoticePeriod`)
- **Pagination & Sorting**: Secure pagination with whitelisted sort fields to prevent invalid property references or SQL injection.
- **Concurrency Control**: Optimistic locking using JPA `@Version` to prevent lost updates in concurrent environments.
- **Robust Error Handling**: Centralized exception handling (`@RestControllerAdvice`) returning standardized RFC-compliant error responses for 400 (Validation), 404 (Not Found), 409 (Conflict/Duplicate), and 500 (Internal Error).
- **Automated Testing**: Unit tests using Mockito and integration/slice tests using Spring `MockMvc` running against an in-memory H2 database.

---

## 🛠 Tech Stack

- **Java**: 17+
- **Framework**: Spring Boot 3.5.x
- **Data Access**: Spring Data JPA / Hibernate
- **Database**: MySQL 8.x (Production/Runtime), H2 (In-memory for Tests)
- **Validation**: Jakarta Bean Validation (Hibernate Validator)
- **Utilities**: Project Lombok
- **Testing**: JUnit 5, Mockito, Spring Test (MockMvc)
- **Build Tool**: Maven

---

## 📋 Prerequisites

Before running the application, ensure you have:
- **JDK 17** or higher installed (`java -version`)
- **MySQL 8.x** installed and running locally
- **Maven 3.8+** (or use your IDE's bundled Maven)

---

## ⚙️ Configuration & Setup

### 1. Database Setup
Create a new database in MySQL:

```sql
CREATE DATABASE candidate_db;
```

### 2. Configure Credentials
Update your database username and password in `src/main/resources/application.properties` (or set them via environment variables):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/candidate_db
spring.datasource.username=root
spring.datasource.password=root
server.port=5001
```

> **Note**: The application runs on port **`5001`** by default.

---

## 🏃 How to Run the Application

### Option A: Using Maven Command Line
```bash
mvn clean spring-boot:run
```

### Option B: From IntelliJ IDEA / Eclipse
1. Open the project in your IDE.
2. Locate `src/main/java/com/nikhil/candidate/CandidateManagementApplication.java`.
3. Right-click and choose **Run 'CandidateManagementApplication'**.

The application will start on: `http://localhost:5001`

---

## 🧪 How to Run Automated Tests

The test suite uses an in-memory **H2 database**, meaning **MySQL does NOT need to be running** to execute tests.

### Run All Tests via CLI:
```bash
mvn test
```

### Run Tests in IDE:
Right-click on `src/test/java` and select **Run 'All Tests'**.

### What the Tests Cover:
1. **`CandidateServiceTest.java` (Unit Tests - Mockito)**:
   - Candidate creation with duplicate email validation.
   - ID retrieval and `ResourceNotFoundException` handling.
   - Dynamic search criteria and pagination logic.
   - Updates and cross-candidate duplicate email checks.
   - Candidate deletion logic.
2. **`CandidateControllerTest.java` (Web Layer Tests - MockMvc)**:
   - HTTP status code verifications (`201 Created`, `200 OK`, `204 No Content`, `400 Bad Request`, `404 Not Found`).
   - Request body validation (`@NotBlank`, `@Email`, `@DecimalMin`).
   - Sorting whitelist validation and pagination parameter validation.

---

## 📖 API Documentation & Endpoints

Base URL: `http://localhost:5001/api/candidates`

### Summary of Endpoints

| HTTP Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/candidates` | Create a new candidate |
| `GET` | `/api/candidates/{id}` | Get candidate details by ID |
| `GET` | `/api/candidates` | Search & filter candidates with pagination and sorting |
| `PUT` | `/api/candidates/{id}` | Update existing candidate details |
| `DELETE` | `/api/candidates/{id}` | Delete a candidate |

---

### Endpoint Details & Examples

#### 1. Create Candidate
- **Endpoint**: `POST /api/candidates`
- **Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1-555-0199",
  "experience": 4.5,
  "location": "Bangalore",
  "currentCompany": "Acme Tech",
  "expectedSalary": 1200000.00,
  "noticePeriod": 30,
  "status": "AVAILABLE"
}
```
- **Response**: `201 Created`
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1-555-0199",
  "experience": 4.5,
  "location": "Bangalore",
  "currentCompany": "Acme Tech",
  "expectedSalary": 1200000.00,
  "noticePeriod": 30,
  "status": "AVAILABLE",
  "createdAt": "2026-11-20T10:15:30",
  "updatedAt": "2026-11-20T10:15:30",
  "version": 0
}
```

---

#### 2. Get Candidate By ID
- **Endpoint**: `GET /api/candidates/{id}`
- **Response**: `200 OK` (or `404 Not Found` if the ID does not exist)

---

#### 3. Search & Filter Candidates (with Pagination & Sorting)
- **Endpoint**: `GET /api/candidates`
- **Supported Query Parameters**:

| Parameter | Type | Example | Description |
| :--- | :--- | :--- | :--- |
| `search` | String | `?search=john` | Partial match across name, email, company |
| `status` | Enum | `?status=AVAILABLE` | `AVAILABLE`, `IN_PROGRESS`, `HIRED`, `REJECTED`, `ON_HOLD` |
| `location` | String | `?location=bangalore` | Case-insensitive partial match |
| `minExperience` | Decimal | `?minExperience=3.0` | Minimum years of experience |
| `maxExperience` | Decimal | `?maxExperience=6.0` | Maximum years of experience |
| `minSalary` | Decimal | `?minSalary=50000` | Minimum expected salary |
| `maxSalary` | Decimal | `?maxSalary=150000` | Maximum expected salary |
| `maxNoticePeriod` | Integer | `?maxNoticePeriod=30` | Maximum notice period in days |
| `page` | Integer | `?page=0` | Page number (default: 0) |
| `size` | Integer | `?size=10` | Records per page (default: 10, max: 100) |
| `sortBy` | String | `?sortBy=experience` | Allowed: `id`, `firstName`, `lastName`, `experience`, `location`, `status`, `expectedSalary`, `noticePeriod`, `createdAt` |
| `direction` | String | `?direction=desc` | `asc` or `desc` (default: `desc`) |

##### Example Queries:
- **Keyword Search**:
  ```bash
  curl "http://localhost:5001/api/candidates?search=acme"
  ```
- **Filter by Status & Location**:
  ```bash
  curl "http://localhost:5001/api/candidates?status=AVAILABLE&location=bangalore"
  ```
- **Complex Search + Multi-Criteria Filter + Pagination + Sort**:
  ```bash
  curl "http://localhost:5001/api/candidates?search=john&status=AVAILABLE&minExperience=2.0&maxNoticePeriod=30&page=0&size=10&sortBy=expectedSalary&direction=asc"
  ```

---

#### 4. Update Candidate
- **Endpoint**: `PUT /api/candidates/{id}`
- **Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.updated@example.com",
  "phone": "+1-555-0199",
  "experience": 5.0,
  "location": "Pune",
  "currentCompany": "NextGen Ltd",
  "expectedSalary": 1400000.00,
  "noticePeriod": 15,
  "status": "IN_PROGRESS"
}
```
- **Response**: `200 OK`

---

#### 5. Delete Candidate
- **Endpoint**: `DELETE /api/candidates/{id}`
- **Response**: `204 No Content`

---

## 🛡️ Error Handling Response Format

All validation and application errors return a consistent JSON structure:

```json
{
  "timestamp": "2026-11-20T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/candidates",
  "validationErrors": {
    "email": "Please provide a valid email address",
    "experience": "Experience cannot be negative"
  }
}
```