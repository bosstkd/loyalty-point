package com.carrefour.kata.presentation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Composition root : point d'entrée Spring Boot du service loyalty-points.
 *
 * <p>Scanne {@code com.carrefour.kata} pour tous les stéréotypes Spring standard.
 * Les services de domaine annotés avec {@code @DomainService} sont enregistrés via
 * {@link com.carrefour.kata.presentation.config.DomainServiceConfiguration}.
 * Les repositories et entités JPA sont configurés via
 * {@link com.carrefour.kata.presentation.config.JpaConfiguration}.
 * Les traitements batch planifiés (expiration des points, notifications) sont câblés dans
 * {@link com.carrefour.kata.presentation.scheduler.PointsLifecycleScheduler}.
 */
@SpringBootApplication(scanBasePackages = "com.carrefour.kata")
@EnableScheduling
public class LoyaltyPointsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoyaltyPointsApplication.class, args);
    }
}
