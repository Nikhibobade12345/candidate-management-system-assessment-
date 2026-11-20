package com.nikhil.candidate.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.nikhil.candidate.dto.CandidateRequest;
import com.nikhil.candidate.dto.CandidateResponse;
import com.nikhil.candidate.dto.CandidateSearchCriteria;
import com.nikhil.candidate.entity.Candidate;
import com.nikhil.candidate.entity.CandidateStatus;
import com.nikhil.candidate.exception.DuplicateResourceException;
import com.nikhil.candidate.exception.ResourceNotFoundException;
import com.nikhil.candidate.repository.CandidateRepository;
import com.nikhil.candidate.servicesImpl.CandidateServiceImpl;

@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

    @Mock
    private CandidateRepository candidateRepository;

    @InjectMocks
    private CandidateServiceImpl candidateService;

    private Candidate candidate;
    private CandidateRequest candidateRequest;

    @BeforeEach
    void setUp() {
        candidate = new Candidate();
        candidate.setId(1L);
        candidate.setFirstName("John");
        candidate.setLastName("Doe");
        candidate.setEmail("john.doe@example.com");
        candidate.setPhone("+1234567890");
        candidate.setExperience(new BigDecimal("5.0"));
        candidate.setLocation("New York");
        candidate.setCurrentCompany("Acme Corp");
        candidate.setExpectedSalary(new BigDecimal("120000.00"));
        candidate.setNoticePeriod(30);
        candidate.setStatus(CandidateStatus.AVAILABLE);

        candidateRequest = new CandidateRequest();
        candidateRequest.setFirstName("John");
        candidateRequest.setLastName("Doe");
        candidateRequest.setEmail("john.doe@example.com");
        candidateRequest.setPhone("+1234567890");
        candidateRequest.setExperience(new BigDecimal("5.0"));
        candidateRequest.setLocation("New York");
        candidateRequest.setCurrentCompany("Acme Corp");
        candidateRequest.setExpectedSalary(new BigDecimal("120000.00"));
        candidateRequest.setNoticePeriod(30);
        candidateRequest.setStatus(CandidateStatus.AVAILABLE);
    }

    // --- CREATE CANDIDATE TESTS ---

    @Test
    @DisplayName("createCandidate: Successfully saves and returns response DTO")
    void createCandidate_Success() {
        when(candidateRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(candidateRepository.saveAndFlush(any(Candidate.class))).thenReturn(candidate);

        CandidateResponse response = candidateService.createCandidate(candidateRequest);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("john.doe@example.com", response.getEmail());
        verify(candidateRepository, times(1)).saveAndFlush(any(Candidate.class));
    }

    @Test
    @DisplayName("createCandidate: Throws DuplicateResourceException when email already exists")
    void createCandidate_DuplicateEmail_ThrowsException() {
        when(candidateRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                candidateService.createCandidate(candidateRequest)
        );

        verify(candidateRepository, never()).saveAndFlush(any(Candidate.class));
    }

    // --- GET CANDIDATE BY ID TESTS ---

    @Test
    @DisplayName("getCandidateById: Returns candidate response when candidate exists")
    void getCandidateById_Success() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));

        CandidateResponse response = candidateService.getCandidateById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John", response.getFirstName());
    }

    @Test
    @DisplayName("getCandidateById: Throws ResourceNotFoundException when candidate ID not found")
    void getCandidateById_NotFound_ThrowsException() {
        when(candidateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                candidateService.getCandidateById(999L)
        );
    }

    // --- GET ALL CANDIDATES (SEARCH & FILTER) TESTS ---

    @Test
    @DisplayName("getAllCandidates: Returns paginated response using criteria specification")
    void getAllCandidates_Success() {
        CandidateSearchCriteria criteria = new CandidateSearchCriteria();
        criteria.setSearch("John");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Candidate> candidatePage = new PageImpl<>(List.of(candidate), pageable, 1);

        when(candidateRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(candidatePage);

        Page<CandidateResponse> result = candidateService.getAllCandidates(criteria, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).getFirstName());
    }

    // --- UPDATE CANDIDATE TESTS ---

    @Test
    @DisplayName("updateCandidate: Successfully updates candidate details")
    void updateCandidate_Success() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(candidateRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), eq(1L))).thenReturn(false);

        candidateRequest.setFirstName("Jane");
        CandidateResponse response = candidateService.updateCandidate(1L, candidateRequest);

        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
        verify(candidateRepository, times(1)).flush();
    }

    @Test
    @DisplayName("updateCandidate: Throws DuplicateResourceException when email used by another candidate")
    void updateCandidate_DuplicateEmail_ThrowsException() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));
        when(candidateRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), eq(1L))).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                candidateService.updateCandidate(1L, candidateRequest)
        );
    }

    // --- DELETE CANDIDATE TESTS ---

    @Test
    @DisplayName("deleteCandidate: Successfully deletes candidate when exists")
    void deleteCandidate_Success() {
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(candidate));

        candidateService.deleteCandidate(1L);

        verify(candidateRepository, times(1)).delete(candidate);
    }

    @Test
    @DisplayName("deleteCandidate: Throws ResourceNotFoundException when candidate does not exist")
    void deleteCandidate_NotFound_ThrowsException() {
        when(candidateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                candidateService.deleteCandidate(999L)
        );

        verify(candidateRepository, never()).delete(any(Candidate.class));
    }

    @Test
    @DisplayName("createCandidate: Trims whitespace and accepts formatted phone up to 20 chars")
    void createCandidate_TrimsWhitespaceAndAcceptsFormattedPhone() {
        when(candidateRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(candidateRepository.saveAndFlush(any(Candidate.class))).thenAnswer(invocation -> {
            Candidate saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        CandidateRequest req = new CandidateRequest();
        req.setFirstName("  Alice  ");
        req.setLastName("  Smith  ");
        req.setEmail("  alice.smith@example.com  ");
        req.setPhone("  +1 (555) 019-9999  ");
        req.setExperience(new BigDecimal("3.5"));
        req.setLocation("  Chicago  ");
        req.setCurrentCompany("  Tech Solutions  ");
        req.setExpectedSalary(new BigDecimal("90000.00"));
        req.setNoticePeriod(15);
        req.setStatus(CandidateStatus.AVAILABLE);

        CandidateResponse res = candidateService.createCandidate(req);

        assertNotNull(res);
        assertEquals("Alice", res.getFirstName());
        assertEquals("Smith", res.getLastName());
        assertEquals("alice.smith@example.com", res.getEmail());
        assertEquals("+1 (555) 019-9999", res.getPhone());
        assertEquals("Chicago", res.getLocation());
        assertEquals("Tech Solutions", res.getCurrentCompany());
    }
}