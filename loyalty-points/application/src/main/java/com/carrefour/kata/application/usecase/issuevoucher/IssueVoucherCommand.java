package com.carrefour.kata.application.usecase.issuevoucher;

/**
 * Commande pour convertir des points de fidélité en bon d'achat.
 */
public record IssueVoucherCommand(String customerId, int points) {
}
