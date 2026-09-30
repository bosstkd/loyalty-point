package com.carrefour.kata.application.usecase.getbalance;

import java.time.LocalDate;
import java.util.List;

/**
 * Résultat d'une requête de solde : solde courant et lots non expirés.
 */
public record GetBalanceResponse(String customerId, int balance, List<LotSummary> lots) {

    public record LotSummary(int remaining, LocalDate expiresAt) {
    }
}
