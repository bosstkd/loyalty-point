package com.carrefour.kata.presentation.scheduler;

import com.carrefour.kata.application.usecase.expirepoints.ExpirePointsCommand;
import com.carrefour.kata.application.usecase.expirepoints.ExpirePointsUseCase;
import com.carrefour.kata.application.usecase.notifyexpiring.NotifyExpiringPointsCommand;
import com.carrefour.kata.application.usecase.notifyexpiring.NotifyExpiringPointsUseCase;
import com.carrefour.kata.domain.account.repository.LoyaltyAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Traitements batch planifiés pour le cycle de vie des points.
 *
 * <p>L'expiration et les notifications d'expiration sont des règles métier pilotées par le temps —
 * elles ne doivent pas dépendre d'une action utilisateur ou d'un appel HTTP. Ce scheduler
 * itère sur tous les comptes et délègue chaque compte au use case approprié,
 * qui encapsule la logique de domaine.</p>
 *
 * <p>Stratégie de planification :
 * <ul>
 *   <li><b>Expiration</b> : s'exécute chaque nuit à minuit — purge les lots dont
 *       la date {@code expiresAt} est dépassée.</li>
 *   <li><b>Notification</b> : s'exécute chaque matin à 08h00 — notifie les clients
 *       dont les points expirent dans les {@value #NOTICE_DAYS} jours à venir.</li>
 * </ul>
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PointsLifecycleScheduler {

    static final int NOTICE_DAYS = 7;

    private final LoyaltyAccountRepository accountRepository;
    private final ExpirePointsUseCase expirePointsUseCase;
    private final NotifyExpiringPointsUseCase notifyExpiringPointsUseCase;

    /**
     * Purge les lots de points expirés de chaque compte de fidélité.
     * S'exécute quotidiennement à minuit (heure du serveur).
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void expirePoints() {
        log.info("Starting nightly points expiration batch");
        var customerIds = accountRepository.findAllCustomerIds();
        log.info("Processing {} accounts", customerIds.size());
        customerIds.forEach(customerId -> {
            try {
                expirePointsUseCase.execute(new ExpirePointsCommand(customerId.value()));
            } catch (Exception e) {
                log.error("Failed to expire points for customer {}: {}", customerId.value(), e.getMessage());
            }
        });
        log.info("Points expiration batch completed");
    }

    /**
     * Envoie des notifications d'expiration imminente aux clients dont les points expirent dans les
     * prochains {@value #NOTICE_DAYS} jours.
     * S'exécute quotidiennement à 08h00 (heure du serveur).
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void notifyExpiringPoints() {
        log.info("Starting daily expiry-soon notification batch (notice window: {} days)", NOTICE_DAYS);
        var customerIds = accountRepository.findAllCustomerIds();
        customerIds.forEach(customerId -> {
            try {
                notifyExpiringPointsUseCase.execute(
                        new NotifyExpiringPointsCommand(customerId.value(), NOTICE_DAYS));
            } catch (Exception e) {
                log.error("Failed to notify expiring points for customer {}: {}", customerId.value(), e.getMessage());
            }
        });
        log.info("Expiry-soon notification batch completed");
    }
}
