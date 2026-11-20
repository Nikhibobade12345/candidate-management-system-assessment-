package com.nikhil.candidate.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import com.nikhil.candidate.dto.CandidateSearchCriteria;
import com.nikhil.candidate.entity.Candidate;
import com.nikhil.candidate.entity.CandidateStatus;
import com.nikhil.candidate.specification.CandidateSpecification;

@DataJpaTest
@ActiveProfiles("test")
class CandidateRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CandidateRepository candidateRepository;

    private Candidate createTestCandidate(String firstName, String lastName, String email, String phone, String location, String company) {
        Candidate candidate = new Candidate();
        candidate.setFirstName(firstName);
        candidate.setLastName(lastName);
        candidate.setEmail(email);
        candidate.setPhone(phone);
        candidate.setExperience(new BigDecimal("5.0"));
        candidate.setLocation(location);
        candidate.setCurrentCompany(company);
        candidate.setExpectedSalary(new BigDecimal("100000.00"));
        candidate.setNoticePeriod(30);
        candidate.setStatus(CandidateStatus.AVAILABLE);
        return candidate;
    }

    @Test
    @DisplayName("Persist candidate with 20-character formatted phone number")
    void persistCandidate_LongFormattedPhone_Success() {
        Candidate candidate = createTestCandidate("John", "Doe", "john.doe@example.com", "+1 (555) 019-9999", "Seattle", "Acme");
        Candidate saved = candidateRepository.saveAndFlush(candidate);

        assertNotNull(saved.getId());
        assertEquals("+1 (555) 019-9999", saved.getPhone());
    }

    @Test
    @DisplayName("Specification search escapes SQL wildcards properly")
    void searchWithWildcardCharacters_EscapesCorrectly() {
        Candidate c1 = createTestCandidate("Bob", "Percent%Person", "bob.percent@example.com", "+1234567890", "Dallas", "Acme 100% Corp");
        Candidate c2 = createTestCandidate("Carol", "Regular", "carol@example.com", "+1234567891", "Dallas", "Regular Corp");

        entityManager.persist(c1);
        entityManager.persist(c2);
        entityManager.flush();

        // Search specifically for "100%"
        CandidateSearchCriteria criteria = new CandidateSearchCriteria();
        criteria.setSearch("100%");

        Specification<Candidate> spec = CandidateSpecification.build(criteria);
        List<Candidate> results = candidateRepository.findAll(spec);

        assertEquals(1, results.size());
        assertEquals("bob.percent@example.com", results.get(0).getEmail());
    }
}
