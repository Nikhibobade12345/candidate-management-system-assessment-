package com.nikhil.candidate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CandidateManagementApplicationTests {

    @Test
    void contextLoads() {
        // Confirms that the Spring application context starts successfully using the test profile (H2).
    }
}
