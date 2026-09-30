package com.carrefour.kata.domain.account;

import com.carrefour.kata.domain.account.vo.LotId;
import com.carrefour.kata.domain.shared.Points;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entité interne de l'agrégat {@link LoyaltyAccount} : un lot de points
 * gagnés à une date donnée avec sa propre date d'expiration.
 *
 * <p>La mutation ({@link #consume(Points)}) est package-private : seule la
 * racine d'agrégat peut modifier un lot, ce qui protège l'invariant FIFO.</p>
 */
public final class PointsLot {

    private final LotId id;
    private Points remaining;
    private final LocalDate earnedAt;
    private final LocalDate expiresAt;

    PointsLot(LotId id, Points remaining, LocalDate earnedAt, LocalDate expiresAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.remaining = Objects.requireNonNull(remaining, "remaining");
        this.earnedAt = Objects.requireNonNull(earnedAt, "earnedAt");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
        if (expiresAt.isBefore(earnedAt)) {
            throw new IllegalArgumentException("expiresAt cannot be before earnedAt");
        }
    }

    /**
     * Point d'entrée de reconstitution pour la couche de persistance.
     */
    public static PointsLot restore(LotId id, Points remaining, LocalDate earnedAt, LocalDate expiresAt) {
        return new PointsLot(id, remaining, earnedAt, expiresAt);
    }

    /**
     * Consomme jusqu'à {@code requested} points de ce lot et retourne
     * le montant effectivement prélevé.
     */
    Points consume(Points requested) {
        Points taken = requested.isGreaterThan(remaining) ? remaining : requested;
        remaining = remaining.subtract(taken);
        return taken;
    }

    public boolean isExpired(LocalDate date) {
        return expiresAt.isBefore(date);
    }

    public boolean isEmpty() {
        return remaining.isZero();
    }

    public LotId id() {
        return id;
    }

    public Points remaining() {
        return remaining;
    }

    public LocalDate earnedAt() {
        return earnedAt;
    }

    public LocalDate expiresAt() {
        return expiresAt;
    }
}
