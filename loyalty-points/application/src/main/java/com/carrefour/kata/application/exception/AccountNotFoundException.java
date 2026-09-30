package com.carrefour.kata.application.exception;

/**
 * Levée quand aucun compte de fidélité n'est trouvé pour l'identifiant client donné.
 */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String customerId) {
        super("No loyalty account found for customer: " + customerId);
    }
}
