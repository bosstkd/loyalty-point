package com.carrefour.kata.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO de requête pour créditer des points sur un compte de fidélité.
 */
public record EarnPointsRequest(
        @Min(1) int points,
        @NotNull LocalDate expiresAt) {
}
