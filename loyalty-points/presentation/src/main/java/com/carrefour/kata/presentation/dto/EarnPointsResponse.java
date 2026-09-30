package com.carrefour.kata.presentation.dto;

import java.time.LocalDate;

/**
 * DTO de réponse pour le point d'entrée de crédit de points.
 */
public record EarnPointsResponse(String customerId, int earnedPoints, int newBalance, LocalDate expiresAt) {
}
