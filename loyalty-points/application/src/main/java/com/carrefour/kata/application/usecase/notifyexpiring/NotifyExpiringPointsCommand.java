package com.carrefour.kata.application.usecase.notifyexpiring;

/**
 * Commande pour notifier un client des points expirant dans noticeDays jours.
 */
public record NotifyExpiringPointsCommand(String customerId, int noticeDays) {
}
