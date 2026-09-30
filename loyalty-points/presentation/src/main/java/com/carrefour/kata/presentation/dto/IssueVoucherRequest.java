package com.carrefour.kata.presentation.dto;

import jakarta.validation.constraints.Min;

/**
 * DTO de requête pour convertir des points en bon d'achat.
 */
public record IssueVoucherRequest(@Min(1) int points) {
}
