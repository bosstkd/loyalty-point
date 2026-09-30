package com.carrefour.kata.application.usecase.earnpoints;

import java.time.LocalDate;

/**
 * Résultat d'une opération de crédit de points réussie.
 */
public record EarnPointsResponse(String customerId, int earnedPoints, int newBalance, LocalDate expiresAt) {
}
