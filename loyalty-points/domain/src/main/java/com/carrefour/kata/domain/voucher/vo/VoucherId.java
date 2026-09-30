package com.carrefour.kata.domain.voucher.vo;

import java.util.UUID;

/**
 * Identité typée d'un bon d'achat.
 */
public record VoucherId(String value) {

    public VoucherId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("VoucherId cannot be blank");
        }
    }

    public static VoucherId of(String value) {
        return new VoucherId(value);
    }

    public static VoucherId newId() {
        return new VoucherId(UUID.randomUUID().toString());
    }
}
