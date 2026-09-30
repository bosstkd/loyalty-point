package com.carrefour.kata.infrastructure;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot configuration for infrastructure integration tests.
 * Provides @AutoConfigurationPackage so that @DataJpaTest can discover JPA entities
 * and repositories in the com.carrefour.kata.infrastructure package.
 */
@SpringBootApplication
class InfrastructureTestApplication {
}
