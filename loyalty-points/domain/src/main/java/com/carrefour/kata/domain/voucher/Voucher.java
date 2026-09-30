package com.carrefour.kata.domain.voucher;

import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.voucher.exception.VoucherNotUsableException;
import com.carrefour.kata.domain.voucher.vo.VoucherId;
import com.carrefour.kata.domain.voucher.vo.VoucherStatus;
import com.carrefour.kata.domain.shared.Points;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Racine d'agrégat : un bon d'achat acheté avec des points de fidélité.
 *
 * <p>Cycle de vie propre (ISSUED, puis USED ou EXPIRED), indépendant du compte
 * de fidélité une fois émis. Lié au client uniquement par son identité.</p>
 */
public final class Voucher {

    private final VoucherId id;
    private final CustomerId customerId;
    private final Points value;
    private final LocalDate issuedAt;
    private final LocalDate expiresAt;
    private VoucherStatus status;

    private Voucher(VoucherId id, CustomerId customerId, Points value,
                    LocalDate issuedAt, LocalDate expiresAt, VoucherStatus status) {
        this.id = Objects.requireNonNull(id, "id");
        this.customerId = Objects.requireNonNull(customerId, "customerId");
        this.value = Objects.requireNonNull(value, "value");
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
        this.status = Objects.requireNonNull(status, "status");
        if (value.isZero()) {
            throw new IllegalArgumentException("A voucher cannot be worth zero points");
        }
        if (expiresAt.isBefore(issuedAt)) {
            throw new IllegalArgumentException("expiresAt cannot be before issuedAt");
        }
    }

    public static Voucher issue(CustomerId customerId, Points value,
                                LocalDate issuedAt, LocalDate expiresAt) {
        return new Voucher(VoucherId.newId(), customerId, value, issuedAt, expiresAt, VoucherStatus.ISSUED);
    }

    /**
     * Point d'entrée de reconstitution pour la couche de domaine depuis la persistance.
     */
    public static Voucher restore(VoucherId id, CustomerId customerId, Points value,
                                  LocalDate issuedAt, LocalDate expiresAt, VoucherStatus status) {
        return new Voucher(id, customerId, value, issuedAt, expiresAt, status);
    }

    /**
     * Marque le bon d'achat comme utilisé.
     *
     * @throws VoucherNotUsableException s'il est déjà USED ou EXPIRED
     */
    public void use() {
        if (status != VoucherStatus.ISSUED) {
            throw new VoucherNotUsableException(id, status);
        }
        status = VoucherStatus.USED;
    }

    /**
     * Marque le bon d'achat comme expiré.
     *
     * @throws VoucherNotUsableException s'il était déjà USED
     */
    public void expire() {
        if (status == VoucherStatus.USED) {
            throw new VoucherNotUsableException(id, status);
        }
        status = VoucherStatus.EXPIRED;
    }

    public boolean isExpired(LocalDate date) {
        return expiresAt.isBefore(date);
    }

    public VoucherId id() {
        return id;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public Points value() {
        return value;
    }

    public LocalDate issuedAt() {
        return issuedAt;
    }

    public LocalDate expiresAt() {
        return expiresAt;
    }

    public VoucherStatus status() {
        return status;
    }
}
