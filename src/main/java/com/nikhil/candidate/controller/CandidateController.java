package com.nikhil.candidate.controller;

import com.nikhil.candidate.dto.CandidateRequest;
import com.nikhil.candidate.dto.CandidateResponse;
import com.nikhil.candidate.dto.CandidateSearchCriteria;
import com.nikhil.candidate.interfaces.CandidateInterface;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateInterface candidateInterface;

    // Constructor injection
    public CandidateController(CandidateInterface candidateInterface) {
        this.candidateInterface = candidateInterface;
    }

    // 1. Create Candidate
    @PostMapping
    public ResponseEntity<CandidateResponse> createCandidate(
            @Valid @RequestBody CandidateRequest request) {

        CandidateResponse response =
                candidateInterface.createCandidate(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 2. Get Candidate By ID
    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> getCandidateById(
            @PathVariable Long id) {

        CandidateResponse response =
                candidateInterface.getCandidateById(id);

        return ResponseEntity.ok(response);
    }

    // 3. Get All Candidates with Pagination and Sorting
    // 3. Get All Candidates with Search, Multi-Criteria Filtering, Pagination, and Sorting
    @GetMapping
    public ResponseEntity<Page<CandidateResponse>> getAllCandidates(
            @ModelAttribute CandidateSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        // Validate pagination values
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        // Whitelist allowed sort fields to prevent SQL injection or bad property references
        if (!sortBy.equals("id")
                && !sortBy.equals("firstName")
                && !sortBy.equals("lastName")
                && !sortBy.equals("experience")
                && !sortBy.equals("location")
                && !sortBy.equals("status")
                && !sortBy.equals("expectedSalary")
                && !sortBy.equals("noticePeriod")
                && !sortBy.equals("createdAt")) {

            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Page<CandidateResponse> response =
                candidateInterface.getAllCandidates(criteria, pageable);

        return ResponseEntity.ok(response);
    }

    // 4. Update Candidate
    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponse> updateCandidate(
            @PathVariable Long id,
            @Valid @RequestBody CandidateRequest request) {

        CandidateResponse response =
                candidateInterface.updateCandidate(id, request);

        return ResponseEntity.ok(response);
    }

    // 5. Delete Candidate
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidate(
            @PathVariable Long id) {

        candidateInterface.deleteCandidate(id);

        return ResponseEntity.noContent().build();
    }
}