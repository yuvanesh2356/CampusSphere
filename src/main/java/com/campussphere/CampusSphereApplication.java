package com.campussphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the CampusSphere application.
 * Bootstraps the embedded server and triggers Spring's component scanning
 * across the com.campussphere base package, picking up every module
 * (auth, config, common, and future modules added in later phases).
 */
@SpringBootApplication
public class CampusSphereApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusSphereApplication.class, args);
    }

}
