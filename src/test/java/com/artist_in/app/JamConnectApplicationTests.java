package com.artist_in.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies the full Spring application context (security, JPA, WebSocket,
 * all controllers/services) wires up correctly. Uses the "test" profile,
 * which points at an in-memory H2 database so no MySQL instance is required
 * to run this test.
 */
@SpringBootTest
@ActiveProfiles("test")
class JamConnectApplicationTests {

    @Test
    void contextLoads() {
        // If the application context fails to start, this test fails.
    }
}
