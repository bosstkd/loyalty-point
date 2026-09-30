package com.carrefour.kata.domain.account.vo;

/**
 * Identité typée d'un client. Le domaine ne détient jamais un objet Customer,
 * uniquement son identité.
 */
public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId cannot be blank");
        }
    }

    public static CustomerId of(String value) {
        return new CustomerId(value);
    }
}
