package com.nikhil.candidate.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.nikhil.candidate.entity.Candidate;


public interface CandidateRepository extends JpaRepository<Candidate, Long>,
        JpaSpecificationExecutor<Candidate> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}