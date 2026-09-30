package com.carrefour.kata.application.usecase.issuevoucher;

import java.time.LocalDate;

/**
 * Résultat d'une émission de bon d'achat réussie.
 */
public record IssueVoucherResponse(
        String voucherId,
        String customerId,
        int value,
        LocalDate issuedAt,
        LocalDate expiresAt) {
}
