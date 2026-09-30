package com.carrefour.kata.application.usecase.expirepoints;

/**
 * Commande pour purger les lots expirés d'un compte de fidélité.
 */
public record ExpirePointsCommand(String customerId) {
}
