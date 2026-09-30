package com.carrefour.kata.presentation.config;

import com.carrefour.kata.domain.shared.DomainService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * Enregistre tous les services de domaine annotés avec {@link DomainService} comme beans Spring.
 * Les services de domaine ne portent aucune annotation Spring — cette configuration comble l'écart.
 */
@Configuration
@ComponentScan(
        basePackages = "com.carrefour.kata.domain",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                classes = DomainService.class
        ),
        useDefaultFilters = false
)
public class DomainServiceConfiguration {
}
