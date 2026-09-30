package com.carrefour.kata.presentation.dto;

/**
 * DTO de réponse pour le point d'entrée de dépense de points.
 */
public record SpendPointsResponse(String customerId, int spentPoints, int remainingBalance) {
}
