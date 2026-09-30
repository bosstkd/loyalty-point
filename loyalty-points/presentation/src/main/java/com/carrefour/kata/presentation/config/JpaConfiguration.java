package com.carrefour.kata.presentation.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;

/**
 * Active les repositories JPA et le scan des entités.
 *
 * <p>Déclarée comme auto-configuration Spring Boot (traitée après les configs utilisateur,
 * après {@link DataSourceAutoConfiguration}) de sorte que :
 * <ul>
 *   <li>{@code @SpringBootTest} — le DataSource est déjà présent → la configuration s'active.</li>
 *   <li>{@code @WebMvcTest} — toutes les auto-configurations non-MVC sont désactivées via
 *       {@code @OverrideAutoConfiguration(enabled=false)} → cette classe n'est jamais chargée.</li>
 * </ul>
 */
@AutoConfiguration(after = DataSourceAutoConfiguration.class)
@ConditionalOnBean(DataSource.class)
@EnableJpaRepositories(basePackages = "com.carrefour.kata.infrastructure.persistence.jpa")
@EntityScan(basePackages = "com.carrefour.kata.infrastructure.persistence.entity")
public class JpaConfiguration {
}
