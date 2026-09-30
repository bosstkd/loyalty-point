package com.carrefour.kata.application.exception;

/**
 * Levée quand aucun bon d'achat n'est trouvé pour l'identifiant donné.
 */
public class VoucherNotFoundException extends RuntimeException {

    public VoucherNotFoundException(String voucherId) {
        super("No voucher found with id: " + voucherId);
    }
}
