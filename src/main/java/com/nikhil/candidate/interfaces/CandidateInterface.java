package com.nikhil.candidate.interfaces;

import com.nikhil.candidate.dto.CandidateSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nikhil.candidate.dto.CandidateRequest;
import com.nikhil.candidate.dto.CandidateResponse;

public interface CandidateInterface {

    CandidateResponse createCandidate(CandidateRequest request);

    CandidateResponse getCandidateById(Long id);

    Page<CandidateResponse> getAllCandidates(CandidateSearchCriteria criteria, Pageable pageable);
    CandidateResponse updateCandidate(Long id, CandidateRequest request);

    void deleteCandidate(Long id);
}