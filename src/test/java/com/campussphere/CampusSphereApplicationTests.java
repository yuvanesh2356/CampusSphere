package com.campussphere;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Minimal smoke test: verifies the full Spring application context
 * (all beans - controllers, services, repositories, security config)
 * wires up and starts without error. Requires a reachable MySQL
 * instance matching application.properties, since ddl-auto=update
 * needs a live connection at startup.
 *
 * Module-specific unit tests (UserServiceTest, etc.) will be added
 * as each module is built out in later phases.
 */
@SpringBootTest
class CampusSphereApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty: a failing context load fails this test.
    }

}
