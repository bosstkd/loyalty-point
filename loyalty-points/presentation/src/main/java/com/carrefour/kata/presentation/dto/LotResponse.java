package com.carrefour.kata.presentation.dto;

import java.time.LocalDate;

/**
 * DTO de réponse représentant un lot de points unique.
 */
public record LotResponse(int remaining, LocalDate expiresAt) {
}
