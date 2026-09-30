package com.carrefour.kata.application.usecase.spendpoints;

/**
 * Résultat d'une opération de dépense de points réussie.
 */
public record SpendPointsResponse(String customerId, int spentPoints, int remainingBalance) {
}
