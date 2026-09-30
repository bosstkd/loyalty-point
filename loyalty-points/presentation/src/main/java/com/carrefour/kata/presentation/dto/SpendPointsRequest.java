package com.carrefour.kata.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requête pour dépenser des points depuis un compte de fidélité.
 */
public record SpendPointsRequest(
        @Min(1) int points,
        @NotBlank String type) {
}
