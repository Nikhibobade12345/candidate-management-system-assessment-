package com.nikhil.candidate.servicesImpl;

import java.util.Locale;

import com.nikhil.candidate.dto.CandidateSearchCriteria;
import com.nikhil.candidate.interfaces.CandidateInterface;
import com.nikhil.candidate.specification.CandidateSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nikhil.candidate.dto.CandidateRequest;
import com.nikhil.candidate.dto.CandidateResponse;
import com.nikhil.candidate.entity.Candidate;
import com.nikhil.candidate.exception.DuplicateResourceException;
import com.nikhil.candidate.exception.ResourceNotFoundException;
import com.nikhil.candidate.repository.CandidateRepository;

@Service
@Transactional(readOnly = true)
public class CandidateServiceImpl implements CandidateInterface {

    private final CandidateRepository candidateRepository;

    // Constructor injection
    public CandidateServiceImpl(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    // Create Candidate
    @Override
    @Transactional
    public CandidateResponse createCandidate(CandidateRequest request) {

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (candidateRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException(
                    "Candidate already exists with email: " + email);
        }

        Candidate candidate = new Candidate();

        candidate.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : null);
        candidate.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        candidate.setEmail(email);
        candidate.setPhone(trimOrNull(request.getPhone()));
        candidate.setExperience(request.getExperience());
        candidate.setLocation(trimOrNull(request.getLocation()));
        candidate.setCurrentCompany(trimOrNull(request.getCurrentCompany()));
        candidate.setExpectedSalary(request.getExpectedSalary());
        candidate.setNoticePeriod(request.getNoticePeriod());
        candidate.setStatus(request.getStatus());

        Candidate savedCandidate = candidateRepository.saveAndFlush(candidate);

        return mapToResponse(savedCandidate);
    }

    // Get Candidate By ID
    @Override
    public CandidateResponse getCandidateById(Long id) {

        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate not found with ID: " + id));

        return mapToResponse(candidate);
    }

    // Get All Candidates with Pagination
    // Get Candidates with Search, Dynamic Filtering, Pagination, and Sorting
    @Override
    public Page<CandidateResponse> getAllCandidates(CandidateSearchCriteria criteria, Pageable pageable) {

        Specification<Candidate> specification = CandidateSpecification.build(criteria);

        return candidateRepository.findAll(specification, pageable)
                .map(this::mapToResponse);
    }

    // Update Candidate
    @Override
    @Transactional
    public CandidateResponse updateCandidate(Long id, CandidateRequest request) {

        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate not found with ID: " + id));

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        boolean emailUsedByAnotherCandidate =
                candidateRepository.existsByEmailIgnoreCaseAndIdNot(email, id);

        if (emailUsedByAnotherCandidate) {
            throw new DuplicateResourceException(
                    "Email already belongs to another candidate");
        }

        candidate.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : null);
        candidate.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        candidate.setEmail(email);
        candidate.setPhone(trimOrNull(request.getPhone()));
        candidate.setExperience(request.getExperience());
        candidate.setLocation(trimOrNull(request.getLocation()));
        candidate.setCurrentCompany(trimOrNull(request.getCurrentCompany()));
        candidate.setExpectedSalary(request.getExpectedSalary());
        candidate.setNoticePeriod(request.getNoticePeriod());
        candidate.setStatus(request.getStatus());

        candidateRepository.flush();

        return mapToResponse(candidate);
    }
    // Delete Candidate
    @Override
    @Transactional
    public void deleteCandidate(Long id) {

        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate not found with ID: " + id));

        candidateRepository.delete(candidate);
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Entity to Response DTO Mapping
    private CandidateResponse mapToResponse(Candidate candidate) {

        CandidateResponse response = new CandidateResponse();

        response.setId(candidate.getId());
        response.setFirstName(candidate.getFirstName());
        response.setLastName(candidate.getLastName());
        response.setEmail(candidate.getEmail());
        response.setPhone(candidate.getPhone());
        response.setExperience(candidate.getExperience());
        response.setLocation(candidate.getLocation());
        response.setCurrentCompany(candidate.getCurrentCompany());
        response.setExpectedSalary(candidate.getExpectedSalary());
        response.setNoticePeriod(candidate.getNoticePeriod());
        response.setStatus(candidate.getStatus());
        response.setCreatedAt(candidate.getCreatedAt());
        response.setUpdatedAt(candidate.getUpdatedAt());
        response.setVersion(candidate.getVersion());
        return response;
    }
}