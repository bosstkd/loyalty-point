package com.carrefour.kata.presentation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration SpringDoc OpenAPI.
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI loyaltyPointsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Loyalty Points API")
                        .description("API de gestion des points de fidélité Carrefour")
                        .version("1.0.0")
                        .contact(new Contact().name("Carrefour Kata")));
    }
}
