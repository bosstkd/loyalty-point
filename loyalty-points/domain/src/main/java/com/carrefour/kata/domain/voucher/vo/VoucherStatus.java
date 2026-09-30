package com.carrefour.kata.domain.voucher.vo;

/**
 * Cycle de vie d'un bon d'achat : émis, puis soit utilisé soit expiré.
 */
public enum VoucherStatus {
    ISSUED,
    USED,
    EXPIRED
}
