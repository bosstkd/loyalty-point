package com.carrefour.kata.presentation.dto;

import java.time.LocalDate;

/**
 * DTO de réponse pour le point d'entrée d'émission de bon d'achat.
 */
public record VoucherResponse(String voucherId, String customerId, int value, LocalDate issuedAt, LocalDate expiresAt) {
}
