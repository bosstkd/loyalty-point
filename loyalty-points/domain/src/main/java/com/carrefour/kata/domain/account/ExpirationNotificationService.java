package com.carrefour.kata.domain.account;

import com.carrefour.kata.domain.account.event.PointsExpiringSoon;
import com.carrefour.kata.domain.shared.DomainService;
import com.carrefour.kata.domain.shared.NotificationPort;

import java.time.LocalDate;
import java.util.List;

/**
 * Service de domaine qui encapsule la règle "notifier le client quand un lot
 * va expirer dans les {@code noticeDays} jours à venir".
 *
 * <p>Le seuil (fenêtre de préavis) est un invariant métier : il appartient ici au
 * domaine, pas dispersé dans les schedulers ou les services applicatifs.</p>
 *
 * <p>Sans état — pas de dépendances de constructeur. Le use case de la couche
 * applicative est responsable de la persistance du compte après l'appel à ce service
 * afin que les événements {@link PointsExpiringSoon} enregistrés puissent être dispatchés.</p>
 */
@DomainService
public final class ExpirationNotificationService {

    /**
     * Analyse le compte à la recherche de lots expirant dans {@code noticeDays} jours et
     * notifie le client pour chaque lot qualifié non vide.
     *
     * @param account          le compte à inspecter
     * @param noticeDays       nombre de jours à anticiper (ex. 7 pour un préavis d'une semaine)
     * @param today            date de référence (déterministe, injectée par le use case)
     * @param notificationPort port utilisé pour dispatcher la notification
     */
    public void notifyExpiringSoon(LoyaltyAccount account, int noticeDays, LocalDate today,
                                   NotificationPort notificationPort) {
        List<PointsLot> expiringSoon = account.lotsExpiringBefore(today.plusDays(noticeDays));
        for (PointsLot lot : expiringSoon) {
            if (!lot.isEmpty()) {
                account.recordExpiringSoon(lot, today);
                notificationPort.notifyExpiringSoon(account.customerId(), lot.remaining(), lot.expiresAt());
            }
        }
    }
}
