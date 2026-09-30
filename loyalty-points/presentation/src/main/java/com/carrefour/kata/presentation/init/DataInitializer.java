package com.carrefour.kata.presentation.init;

import com.carrefour.kata.application.usecase.earnpoints.EarnPointsCommand;
import com.carrefour.kata.application.usecase.earnpoints.EarnPointsUseCase;
import com.carrefour.kata.domain.account.repository.LoyaltyAccountRepository;
import com.carrefour.kata.domain.account.vo.CustomerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Initialise un compte de fidélité par défaut ({@code cust-123}) au démarrage afin que
 * l'API REST et l'interface frontend puissent être exercées sans configuration manuelle.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private static final String DEFAULT_CUSTOMER_ID = "cust-123";
    private static final int    INITIAL_POINTS       = 500;
    private static final LocalDate EXPIRES_AT        = LocalDate.now().plusYears(1);

    private final LoyaltyAccountRepository accountRepository;
    private final EarnPointsUseCase earnPointsUseCase;

    @Override
    public void run(ApplicationArguments args) {
        if (accountRepository.findByCustomerId(CustomerId.of(DEFAULT_CUSTOMER_ID)).isPresent()) {
            log.info("Default account '{}' already exists — skipping seed", DEFAULT_CUSTOMER_ID);
            return;
        }

        earnPointsUseCase.execute(new EarnPointsCommand(DEFAULT_CUSTOMER_ID, INITIAL_POINTS, EXPIRES_AT));
        log.info("Seeded default account '{}' with {} points (expires {})",
                DEFAULT_CUSTOMER_ID, INITIAL_POINTS, EXPIRES_AT);
    }
}
