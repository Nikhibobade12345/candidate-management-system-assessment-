# AI-Assisted Development Process

This document describes how AI was used during the development of this assignment. 

As stated in the assignment instructions, AI tools were permitted as a developer productivity tool. My approach was to **lead the architecture, design, and coding myself**, using AI strictly as a consultative sounding board and productivity assistant (similar to how a developer uses StackOverflow, documentation, or GitHub Copilot).

---

## 1. Where I Used AI

I wrote the core business logic, entity models, and REST endpoints myself. I used AI in a few specific, targeted areas:
- **Test Scaffolding & Edge Cases**: Brainstorming test boundary conditions (e.g., verifying behavior on duplicate email edge cases, negative page sizes, and sorting by unmapped fields).
- **Regex & Validation Formatting**: Reviewing the phone number validation regex (`^[+]?[0-9\s()-]{7,20}$`) to support common international formats.
- **Mock Data Generation**: Creating realistic seed data (sample candidates with diverse names, cities, and salaries) to test pagination and performance.
- **Dependency Sanity Check**: Double-checking `pom.xml` configurations for in-memory H2 test dependencies.

---

## 2. How I Used It

I used AI interactively for quick reference lookups, much like consulting framework documentation:
- Asking for standard patterns for mapping Spring Data JPA `Specification` predicates dynamically.
- Verifying the difference between `@ModelAttribute` and `@RequestParam` when binding query parameters to a DTO in Spring MVC `GET` requests.
- Asking for edge-case ideas that could cause race conditions in concurrent update scenarios.

---

## 3. How I Validated Its Output

No AI suggestion was accepted blindly. Everything was validated through practical engineering checks:
1. **Manual Implementation**: I wrote and typed all code into my IDE myself, reviewing each line to ensure it adhered to clean coding standards and Spring Boot conventions.
2. **Compilation & IDE Inspections**: Verified that all imports, annotations, and types compiled cleanly without warnings.
3. **Automated Tests**: Wrote and executed unit tests (`CandidateServiceTest`) and controller slice tests (`CandidateControllerTest`) to verify that the application correctly returns expected status codes (`201`, `200`, `204`, `400`, `404`, `409`).
4. **Manual API & Database Verification**: Tested endpoints in the browser and Postman, checking database records in MySQL to verify persistence, auditing timestamps, and version increments.

---

## 4. What Changes I Made to AI-Generated Output

Whenever I reviewed an AI suggestion, I adapted and refined it to fit my design:
- **Encapsulated DTOs**: Rather than cluttering the controller method with 8 individual query parameters, I encapsulated all filter inputs into a dedicated `CandidateSearchCriteria` DTO.
- **Strict Sort Whitelisting**: Generic AI pagination suggestions typically pass raw sort strings directly to the repository. I added explicit whitelisting in `CandidateController` (`id`, `firstName`, `lastName`, `experience`, `location`, `status`, `expectedSalary`, `noticePeriod`, `createdAt`) to prevent invalid property reference errors or injection attacks.
- **Customized Error Responses**: I structured the `GlobalExceptionHandler` to extract individual field validation errors into a clean key-value map (`fieldName -> errorMessage`), rather than returning generic error strings.

---

## 5. Where I Chose NOT to Follow an AI Recommendation and Why

1. **Rejected Automated Code Generation / Script-Writing**:
   - I chose not to let AI generate large code blocks or write files automatically.
   - **Reason**: I wanted complete personal mastery over every class, interface, and method so that I can confidently explain, debug, or extend any part of the codebase during the technical evaluation.
2. **Rejected Premature Elasticsearch Setup**:
   - An AI tool suggested adding Elasticsearch for search and filtering.
   - **Reason**: For a standalone assignment, running an Elasticsearch cluster alongside MySQL adds unnecessary complexity, container overhead, and dual-write issues. Spring Data JPA Specifications handle dynamic searching and filtering cleanly and natively inside MySQL. I documented Elasticsearch as a theoretical Phase 2 scaling step in `DESIGN.md` instead.
3. **Chose Explicit Hard Deletes over Soft Deletes**:
   - An AI suggestion recommended soft deletes (`@SQLDelete` with `is_deleted`).
   - **Reason**: Soft deletes introduce complications with database unique constraints (e.g., if a candidate with a previously deleted email re-applies). For the scope of this assignment, standard transactional hard deletes paired with optimistic locking provided the cleanest, most maintainable solution.
