package com.nikhil.candidate.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikhil.candidate.dto.CandidateRequest;
import com.nikhil.candidate.dto.CandidateResponse;
import com.nikhil.candidate.entity.CandidateStatus;
import com.nikhil.candidate.exception.ResourceNotFoundException;
import com.nikhil.candidate.interfaces.CandidateInterface;

@WebMvcTest(CandidateController.class)
class CandidateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CandidateInterface candidateInterface;

    // Helper method to create a valid request
    private CandidateRequest createValidRequest() {
        CandidateRequest req = new CandidateRequest();
        req.setFirstName("Alice");
        req.setLastName("Smith");
        req.setEmail("alice.smith@example.com");
        req.setPhone("+1234567890");
        req.setExperience(new BigDecimal("4.5"));
        req.setLocation("San Francisco");
        req.setCurrentCompany("Tech Corp");
        req.setExpectedSalary(new BigDecimal("95000.00"));
        req.setNoticePeriod(15);
        req.setStatus(CandidateStatus.AVAILABLE);
        return req;
    }

    // Helper method to create a response
    private CandidateResponse createResponse() {
        CandidateResponse res = new CandidateResponse();
        res.setId(1L);
        res.setFirstName("Alice");
        res.setLastName("Smith");
        res.setEmail("alice.smith@example.com");
        res.setPhone("+1234567890");
        res.setExperience(new BigDecimal("4.5"));
        res.setLocation("San Francisco");
        res.setCurrentCompany("Tech Corp");
        res.setExpectedSalary(new BigDecimal("95000.00"));
        res.setNoticePeriod(15);
        res.setStatus(CandidateStatus.AVAILABLE);
        return res;
    }

    // --- CREATE CANDIDATE TESTS ---

    @Test
    @DisplayName("POST /api/candidates: Valid request returns 201 Created")
    void createCandidate_ValidRequest_Returns201() throws Exception {
        CandidateRequest request = createValidRequest();
        CandidateResponse response = createResponse();

        when(candidateInterface.createCandidate(any(CandidateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice.smith@example.com"));
    }

    @Test
    @DisplayName("POST /api/candidates: Invalid request (missing required fields) returns 400 Bad Request")
    void createCandidate_InvalidRequest_Returns400() throws Exception {
        CandidateRequest invalidRequest = new CandidateRequest(); // Empty fields violate @NotBlank & @NotNull

        mockMvc.perform(post("/api/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.firstName").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    // --- GET CANDIDATE BY ID TESTS ---

    @Test
    @DisplayName("GET /api/candidates/{id}: Existing candidate returns 200 OK")
    void getCandidateById_Found_Returns200() throws Exception {
        when(candidateInterface.getCandidateById(1L)).thenReturn(createResponse());

        mockMvc.perform(get("/api/candidates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    @DisplayName("GET /api/candidates/{id}: Non-existing candidate returns 404 Not Found")
    void getCandidateById_NotFound_Returns404() throws Exception {
        when(candidateInterface.getCandidateById(999L))
                .thenThrow(new ResourceNotFoundException("Candidate not found with ID: 999"));

        mockMvc.perform(get("/api/candidates/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Candidate not found with ID: 999"));
    }

    // --- GET ALL CANDIDATES (SEARCH & FILTER) TESTS ---

    @Test
    @DisplayName("GET /api/candidates: With search and filter params returns 200 OK")
    void getAllCandidates_WithFilters_Returns200() throws Exception {
        Page<CandidateResponse> page = new PageImpl<>(List.of(createResponse()), PageRequest.of(0, 10), 1);
        when(candidateInterface.getAllCandidates(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/candidates")
                        .param("search", "Alice")
                        .param("status", "AVAILABLE")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "firstName")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Alice"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/candidates: Invalid sort field returns 400 Bad Request")
    void getAllCandidates_InvalidSortField_Returns400() throws Exception {
        mockMvc.perform(get("/api/candidates")
                        .param("sortBy", "unauthorizedField"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid sort field: unauthorizedField"));
    }

    // --- DELETE CANDIDATE TESTS ---

    @Test
    @DisplayName("DELETE /api/candidates/{id}: Successfully deletes and returns 204 No Content")
    void deleteCandidate_Success_Returns204() throws Exception {
        doNothing().when(candidateInterface).deleteCandidate(1L);

        mockMvc.perform(delete("/api/candidates/1"))
                .andExpect(status().isNoContent());

        verify(candidateInterface, times(1)).deleteCandidate(1L);
    }

    @Test
    @DisplayName("POST /api/candidates: Invalid phone format returns 400 Bad Request")
    void createCandidate_InvalidPhone_Returns400() throws Exception {
        CandidateRequest request = createValidRequest();
        request.setPhone("invalid-phone-abc");

        mockMvc.perform(post("/api/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.phone").value("Please provide a valid phone number"));
    }

    @Test
    @DisplayName("GET /api/candidates: Whitelisted sort by updatedAt returns 200 OK")
    void getAllCandidates_SortByUpdatedAt_Returns200() throws Exception {
        Page<CandidateResponse> page = new PageImpl<>(List.of(createResponse()), PageRequest.of(0, 10), 1);
        when(candidateInterface.getAllCandidates(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/candidates")
                        .param("sortBy", "updatedAt")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Alice"));
    }

    @Test
    @DisplayName("GET /api/candidates: Inverted experience range returns 400 Bad Request")
    void getAllCandidates_InvertedExperienceRange_Returns400() throws Exception {
        mockMvc.perform(get("/api/candidates")
                        .param("minExperience", "10.0")
                        .param("maxExperience", "2.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("minExperience cannot be greater than maxExperience"));
    }

    @Test
    @DisplayName("GET /api/candidates: Inverted salary range returns 400 Bad Request")
    void getAllCandidates_InvertedSalaryRange_Returns400() throws Exception {
        mockMvc.perform(get("/api/candidates")
                        .param("minSalary", "150000.00")
                        .param("maxSalary", "50000.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("minSalary cannot be greater than maxSalary"));
    }

    @Test
    @DisplayName("GET /api/candidates: Invalid sort direction returns 400 Bad Request")
    void getAllCandidates_InvalidDirection_Returns400() throws Exception {
        mockMvc.perform(get("/api/candidates")
                        .param("direction", "sideways"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}