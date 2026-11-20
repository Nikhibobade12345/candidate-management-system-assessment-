# Technical & Design Document: Candidate Management System

## 1. Executive Summary & Design Philosophy
The Candidate Management System is a robust RESTful backend service engineered using **Java 17** and **Spring Boot 3.5**. It provides recruitment and talent acquisition teams with full lifecycle management, dynamic multi-criteria search, and safe concurrent access to candidate profiles.

The architecture emphasizes four core software engineering principles:
1. **Separation of Concerns**: Strict boundary enforcement between presentation, business rules, and persistence.
2. **Defensive API Design**: Preventing mass-assignment vulnerabilities, SQL injection, and invalid sort property injections.
3. **Data Integrity & Concurrency**: Using optimistic locking to prevent "lost updates" without database locking overhead.
4. **Test Portability**: Automated tests decoupled from external infrastructure, executing against an in-memory database with zero external setup.

---

## 2. Layered Architecture

The application adopts a **3-tier Layered Architecture** with unidirectional data flow:

```
[ HTTP Clients / Postman / Frontend ]
                 │
                 ▼
     ┌───────────────────────┐
     │  CandidateController  │  ◀── Web Layer: HTTP routing, deserialization, sort whitelisting
     └───────────┬───────────┘
                 │
                 ▼
     ┌───────────────────────┐
     │  CandidateInterface   │  ◀── Service Contract: Abstraction (Dependency Inversion)
     └───────────┬───────────┘
                 │
                 ▼
     ┌───────────────────────┐
     │ CandidateServiceImpl  │  ◀── Business Logic: Email normalization, deduplication, transactions
     └───────────┬───────────┘
                 │
                 ▼
     ┌───────────────────────┐
     │  CandidateRepository  │  ◀── Persistence Layer: Spring Data JPA + Specifications
     └───────────┬───────────┘
                 │
                 ▼
      [ Relational Database ]
      (MySQL in Prod / H2 in Tests)
```

### Key Architectural Decisions:
- **Interface-Driven Service Layer (`CandidateInterface`)**:
  Adheres to the **Dependency Inversion Principle (DIP)**. The controller depends upon an abstraction rather than a concrete implementation, allowing alternate implementations (e.g., caching decorators or mock services in unit tests) without altering consumer code.
- **DTO (Data Transfer Object) Pattern**:
  Database entities (`Candidate`) are strictly isolated from the presentation layer. Exposing entities directly leads to mass-assignment security vulnerabilities (over-posting) and tightly couples API contracts to database table schemas. We separate this into:
  - `CandidateRequest`: Enforces validation rules on incoming payloads.
  - `CandidateResponse`: Exposes a stable, sanitized API view.
  - `CandidateSearchCriteria`: Encapsulates optional filtering and search parameters.

---

## 3. Data Modeling & Database Design

### Database Table: `candidates`

| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `first_name` | `VARCHAR(100)` | NOT NULL | Candidate's given name |
| `last_name` | `VARCHAR(100)` | NOT NULL | Candidate's family name |
| `email` | `VARCHAR(150)` | NOT NULL, UNIQUE | Deduplication and contact key |
| `phone` | `VARCHAR(15)` | NULL | Contact phone number |
| `experience` | `DECIMAL(4,1)` | NULL | Years of experience (e.g., `5.5`) |
| `location` | `VARCHAR(100)` | NULL | City or geographical region |
| `current_company`| `VARCHAR(150)` | NULL | Current employer |
| `expected_salary`| `DECIMAL(12,2)`| NULL | Expected compensation |
| `notice_period` | `INT` | NULL | Notice period in days |
| `status` | `VARCHAR(30)` | NOT NULL | Enum: `AVAILABLE`, `IN_PROGRESS`, `HIRED`, `REJECTED`, `ON_HOLD` |
| `created_at` | `DATETIME(6)` | NOT NULL | Auditing timestamp (record creation) |
| `updated_at` | `DATETIME(6)` | NOT NULL | Auditing timestamp (last update) |
| `version` | `BIGINT` | NOT NULL | Optimistic locking token |

### Engineering Considerations:
1. **Precision with `BigDecimal`**:
   `expected_salary` (`DECIMAL(12,2)`) and `experience` (`DECIMAL(4,1)`) are mapped to Java's `BigDecimal`. Primitive `float` and `double` use binary floating-point arithmetic (IEEE 754), which causes rounding inaccuracies when storing financial compensation.
2. **Indexing Strategy**:
   - `uk_candidate_email`: Unique B-Tree index ensuring $O(1)$ duplicate checking and lookup.
   - `idx_candidate_status`: Accelerates filtering by candidate pipeline stage.
   - `idx_candidate_location`: Speeds up geographical candidate searches.
   - `idx_candidate_experience`: Optimizes numerical range scans (`minExperience` to `maxExperience`).
3. **Concurrency Control (Optimistic Locking)**:
   In recruitment CRMs, multiple hiring managers and recruiters frequently interact with the same candidate profile. Using pessimistic locks (`SELECT ... FOR UPDATE`) reduces database throughput and risks deadlocks. Instead, JPA `@Version` optimistic locking is implemented. If concurrent updates occur, Hibernate detects version divergence and raises `OptimisticLockingFailureException`, which our global handler maps cleanly to HTTP `409 Conflict`.
4. **Automated Auditing**:
   Spring Data JPA's `@EntityListeners(AuditingEntityListener.class)` automatically updates `createdAt` and `updatedAt`, providing audit trails without manual boilerplate.

---

## 4. Search and Multi-Criteria Filtering Strategy

### Why Spring Data JPA Specifications?
Writing static query methods (e.g. `findByStatusAndLocationAndExperience...`) causes a combinatorial explosion of repository methods as new filters are introduced.

Instead, the application implements the **Specification Pattern** using the JPA Criteria API (`CandidateSpecification`):

```java
Specification<Candidate> spec = CandidateSpecification.build(criteria);
return candidateRepository.findAll(spec, pageable).map(this::mapToResponse);
```

### Query Execution Mechanics:
1. **Dynamic Predicate Construction**:
   Filters are dynamically appended to an `ArrayList<Predicate>` only if present in `CandidateSearchCriteria`:
   - **Keyword Search**: Performs case-insensitive matching across `firstName`, `lastName`, `email`, and `currentCompany` using `criteriaBuilder.like(criteriaBuilder.lower(...), "%" + search + "%")` joined with `OR`.
   - **Exact Matching**: Filters by `status` enum.
   - **Partial Matching**: Location string matching.
   - **Range Matching**: Numerical comparisons on `experience` and `expectedSalary` using `greaterThanOrEqualTo` and `lessThanOrEqualTo`.
   - **Notice Period Threshold**: `noticePeriod <= maxNoticePeriod`.
2. **Sort Whitelisting**:
   To prevent SQL injection or application crashes via invalid entity property references, `CandidateController` explicitly validates `sortBy` against an allowed whitelist:
   `id`, `firstName`, `lastName`, `experience`, `location`, `status`, `expectedSalary`, `noticePeriod`, `createdAt`.
3. **Memory-Safe Pagination**:
   `page` and `size` parameters are validated (`0 <= page`, `1 <= size <= 100`) to prevent out-of-memory errors caused by unbounded queries.

---

## 5. Error Handling & Validation Architecture

The application implements a centralized, RFC-compliant error reporting mechanism via `GlobalExceptionHandler` (`@RestControllerAdvice`).

### Standardized Error Format:
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

### Exception Mapping:
- **`400 Bad Request`**:
  - `MethodArgumentNotValidException`: Bean validation failures (`@NotBlank`, `@Email`, `@DecimalMin`, `@Size`).
  - `MethodArgumentTypeMismatchException`: Path variable type errors (e.g. `/api/candidates/abc`).
  - `BindException`: Query parameter binding/enum errors (e.g. `?status=INVALID`).
  - `HttpMessageNotReadableException`: Malformed JSON syntax in request bodies.
  - `IllegalArgumentException`: Out-of-bounds pagination or unauthorized sort parameters.
- **`404 Not Found`**:
  - `ResourceNotFoundException`: Candidate ID does not exist in the database.
- **`409 Conflict`**:
  - `DuplicateResourceException`: Attempting to register an email already in use.
  - `DataIntegrityViolationException`: Database-level unique constraint collision.
  - `OptimisticLockingFailureException`: Concurrent modification conflicts.
- **`500 Internal Server Error`**:
  - General `Exception`: Caught and logged via SLF4J with stack trace while shielding internal implementation details from clients.

---

## 6. Automated Testing Strategy

The test suite is structured around the standard **Test Pyramid**, achieving fast execution and high confidence:

```
        /  Web Layer Tests (MockMvc)  \      <-- CandidateControllerTest
       /───────────────────────────────\
      /   Unit Tests (JUnit 5 + Mockito)\    <-- CandidateServiceTest
     /───────────────────────────────────\
    /      In-Memory H2 Test Database      \  <-- application-test.properties
```

1. **Service Unit Tests (`CandidateServiceTest`)**:
   - Uses **JUnit 5** and **Mockito** (`@ExtendWith(MockitoExtension.class)`).
   - Fast execution (runs in < 1 second without booting Spring context).
   - Tests business logic branches: successful creation, duplicate email rejection, not-found exceptions, dynamic criteria execution, and update integrity.
2. **Controller Slice Tests (`CandidateControllerTest`)**:
   - Uses **`@WebMvcTest`** and **`MockMvc`**.
   - Validates JSON serialization, HTTP status code contracts (`201`, `200`, `204`, `400`, `404`), Bean validation constraints, and sort whitelist validation.
3. **Test Portability**:
   - Configured with `@ActiveProfiles("test")` reading from `src/test/resources/application-test.properties`.
   - Executes against an embedded **H2 in-memory database**, ensuring `mvn test` passes cleanly on any machine or CI/CD runner without a local MySQL service.

---

## 7. Scalability & Growth Evolution Strategy

*(Addressing the Assessment Requirement: Scaling for large volumes of records and users)*

As the platform scales to **millions of candidate records** and **thousands of concurrent recruiters**, a single relational database instance will hit I/O and CPU bottlenecks. Below is the pragmatic evolution strategy:

```
                               ┌─────────────────────────────────┐
                               │   API Gateway & Load Balancer   │ (Rate Limiting, JWT Auth)
                               └───────────────┬─────────────────┘
                                               │
                                               ▼
                     ┌─────────────────────────────────────────────────────┐
                     │ Candidate Service Pods (Horizontal Auto-scaling)    │
                     └─────────────┬─────────────────────────┬─────────────┘
                                   │                         │
                 ┌─────────────────┴───────┐                 │
                 ▼                         ▼                 ▼
     ┌───────────────────────┐ ┌───────────────────────┐ ┌───────────────────────┐
     │  Redis Caching Tier   │ │ MySQL (Read Replicas) │ │ Elasticsearch Cluster │
     │ (Hot candidate profiles)│ │ (Reporting & Reads)   │ │ (Full-text & Filters) │
     └───────────────────────┘ └───────────────────────┘ └───────────▲───────────┘
                                           ▲                         │
                                           │                         │ (Async CDC sync)
                               ┌───────────┴───────────┐             │
                               │ MySQL Primary (Writes)│ ────────────┘
                               └───────────────────────┘ (Kafka / Debezium)
```

### Step 1: Read/Write Database Splitting & Read Replicas
- Recruitment platforms are read-intensive (90% read/search, 10% write/update).
- Configure a **Primary MySQL node** exclusively for writes (`POST`, `PUT`, `DELETE`) and deploy multiple **Read Replicas** for query traffic.
- Use Spring's `AbstractRoutingDataSource` with `@Transactional(readOnly = true)` to route queries to replicas transparently.

### Step 2: Distributed Caching with Redis
- Cache frequently viewed candidate profiles and lookup tables using **Redis** (Cache-Aside pattern).
- Set explicit TTLs (Time-To-Live) and invalidate keys upon updates or deletions to guarantee data freshness.

### Step 3: Dedicated Inverted-Index Search (Elasticsearch / OpenSearch)
- **Bottleneck**: SQL `LIKE '%term%'` triggers full-table index scans. At millions of records, multi-criteria filtering with multiple `LIKE` conditions degrades query latency.
- **Solution**: Offload keyword search and multi-criteria filtering to **Elasticsearch**:
  - Inverted indexes deliver sub-50ms search times across candidate names, resumes, skills, and companies.
  - Synchronize data asynchronously from MySQL to Elasticsearch using **Change Data Capture (CDC)** via **Debezium** and **Apache Kafka**, avoiding dual-write inconsistencies.

### Step 4: Asynchronous Task Execution (Message Queues)
- Offload non-critical operations from the HTTP request cycle using **Apache Kafka** or **RabbitMQ**:
  - Sending status-change email notifications to candidates.
  - Resume parsing and metadata extraction.
  - Audit logging and recruitment activity streams.

### Step 5: Containerization & Cloud Orchestration
- Package the Spring Boot service into lightweight Docker containers.
- Deploy onto **Kubernetes (K8s)** with Horizontal Pod Autoscaling (HPA) configured based on CPU utilization and incoming HTTP request rates.
