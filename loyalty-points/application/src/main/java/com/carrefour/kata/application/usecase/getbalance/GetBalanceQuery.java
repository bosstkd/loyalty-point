package com.carrefour.kata.application.usecase.getbalance;

/**
 * Requête pour récupérer le solde et la liste des lots d'un compte de fidélité.
 */
public record GetBalanceQuery(String customerId) {
}
