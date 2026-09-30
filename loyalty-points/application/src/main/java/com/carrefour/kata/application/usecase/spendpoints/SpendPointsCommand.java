package com.carrefour.kata.application.usecase.spendpoints;

import com.carrefour.kata.domain.account.vo.SpendingType;

/**
 * Commande pour dépenser des points depuis un compte de fidélité (PAYMENT ou DONATION uniquement).
 * Pour la conversion en bon d'achat, utiliser IssueVoucherUseCase.
 */
public record SpendPointsCommand(String customerId, int points, SpendingType type) {
}
