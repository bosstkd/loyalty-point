package com.carrefour.kata.application.usecase.earnpoints;

import java.time.LocalDate;

/**
 * Commande pour créditer des points sur le compte de fidélité d'un client.
 */
public record EarnPointsCommand(String customerId, int points, LocalDate expiresAt) {
}
