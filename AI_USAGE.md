# AI-Assisted Development Process

This document details the AI-assisted development methodology utilized during the engineering of the Candidate Management System. It transparently explains how AI was leveraged as a developer productivity tool, how outputs were critically validated, and where AI suggestions were refined or rejected.

---

## 1. Where I Used AI

AI tools were integrated across key phases of the software development lifecycle:

- **Requirements Decomposition & Gap Analysis**:
  Cross-referencing the project requirements document against the initial starter codebase to identify missing functional capabilities (specifically dynamic multi-criteria filtering, search, and automated test coverage).
- **Architectural & Design Exploration**:
  Evaluating design patterns for dynamic queries in Spring Data JPA (comparing hardcoded repository method queries, QueryDSL, and JPA Criteria Specifications).
- **Test Suite Scaffolding**:
  Generating baseline boilerplate for Mockito unit tests and MockMvc web slice tests to accelerate test authoring.
- **Dependency & Hygiene Auditing**:
  Reviewing `pom.xml` for duplicate dependencies and ensuring proper test-scoped in-memory database configurations.

---

## 2. How I Used It

Rather than relying on AI to generate code end-to-end without oversight, I used AI in a focused, prompt-driven, and consultative manner:

- **Interactive Design Queries**:
  Asked targeted questions on Spring Boot best practices, such as:
  > *"How should we design a clean DTO structure for dynamic multi-criteria search with pagination without polluting controller signatures with 8+ `@RequestParam`s?"*
- **Conceptual Clarification**:
  Sought in-depth explanations of underlying framework mechanics (e.g., understanding why `@ModelAttribute` is the proper pattern for binding query parameters into a POJO on `GET` requests compared to `@RequestBody`).
- **Edge-Case Brainstorming**:
  Prompted for realistic edge cases that could break the application in production (such as optimistic locking collisions, email case-sensitivity mismatches, and parameter type mismatches on path variables).

---

## 3. How I Validated Its Output

To uphold strict software engineering standards, every AI-generated suggestion underwent rigorous, multi-stage validation before being committed:

1. **Manual Code Inspection & Line-by-Line Review**:
   - Every class, method, and annotation was manually inspected to verify adherence to SOLID principles, clean architecture, and correct Spring Boot conventions.
2. **Static Typing & Compilation Verification**:
   - Ensured clean compilation in IntelliJ IDEA with zero compiler warnings, missing imports, or deprecated API usages.
3. **Automated Test Verification**:
   - Ran unit test suites (`CandidateServiceTest`) and web slice tests (`CandidateControllerTest`) to programmatically verify business logic branches:
     - Verifying that duplicate emails throw `DuplicateResourceException`.
     - Verifying that nonexistent IDs return `404 Not Found`.
     - Verifying that invalid bean validation constraints return structured `400 Bad Request` messages.
4. **Interactive Manual API Testing**:
   - Executed live HTTP calls via Postman and `curl` against running application instances, systematically testing edge-case query parameters (e.g., `?search=john&status=AVAILABLE`, invalid sort fields, and boundary page sizes).

---

## 4. What Changes I Made to AI-Generated Output

AI recommendations were actively adapted and improved to meet production standards:

- **Encapsulated Search Criteria DTO (`CandidateSearchCriteria`)**:
  Initial AI suggestions often proposed passing multiple `@RequestParam` values directly into controller methods. I rejected this in favor of a clean `CandidateSearchCriteria` DTO, eliminating the "Long Parameter List" code smell.
- **Strict Sort Whitelisting**:
  AI-generated pagination code typically passed raw sort strings directly to `Sort.by(sortBy)`. Because this allows users to reference unmapped entity properties or cause database exceptions, I introduced explicit sort field whitelisting in `CandidateController`.
- **Structured Field-Level Error Formatting**:
  Default AI exception handlers often returned generic validation error strings. I refined `GlobalExceptionHandler` to extract all `FieldError` instances into a clean `Map<String, String>` mapping fields directly to their validation error messages.
- **Port-Variable & Type Mismatch Handlers**:
  Extended the global exception handler to intercept `MethodArgumentTypeMismatchException` and `BindException` to ensure type errors on path variables (e.g., `/api/candidates/abc`) return clean `400 Bad Request` rather than unhandled `500 Internal Server Errors`.

---

## 5. Where I Chose NOT to Follow an AI Recommendation and Why

1. **Rejected Automated Code Modification**:
   - **Recommendation**: AI coding tools frequently offer to automatically insert or rewrite codebase files.
   - **Decision**: I explicitly declined automatic modification and insisted on writing and integrating code step-by-step.
   - **Rationale**: The assessment emphasizes deep personal understanding. Manually authoring and reviewing each class guarantees full mastery, ensuring I can confidently explain, debug, or extend any part of the codebase during technical interview discussions.
2. **Deferred Premature Elasticsearch Integration**:
   - **Recommendation**: AI initially suggested incorporating a dedicated Elasticsearch cluster for candidate search.
   - **Decision**: I chose to implement dynamic search using Spring Data JPA Specifications within MySQL.
   - **Rationale**: Introducing Elasticsearch for a single-service assessment introduces significant infrastructure overhead, container requirements, and dual-write complexity. Spring Data JPA Specifications fulfill all keyword search and multi-criteria filter requirements elegantly within the existing relational database. I documented Elasticsearch as a Phase 2 scalability evolution in `DESIGN.md` instead of prematurely over-engineering the current solution.
3. **Rejected Soft Deletes in Favor of Explicit Hard Deletes**:
   - **Recommendation**: AI suggested adding `@SQLDelete` soft deletes with an `is_deleted` flag.
   - **Decision**: Maintained standard JPA hard deletes.
   - **Rationale**: Soft deletes complicate unique constraints (e.g., allowing a candidate with a previously deleted email to re-apply) and require custom filtering across all queries. For the scope of this assignment, standard transactional deletes coupled with optimistic locking provided the cleanest, most maintainable solution.
