package com.carrefour.kata.domain.account.vo;

import java.util.UUID;

/**
 * Identité typée d'un lot de points.
 */
public record LotId(String value) {

    public LotId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("LotId cannot be blank");
        }
    }

    public static LotId of(String value) {
        return new LotId(value);
    }

    public static LotId newId() {
        return new LotId(UUID.randomUUID().toString());
    }
}
