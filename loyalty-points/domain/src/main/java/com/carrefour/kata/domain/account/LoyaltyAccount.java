package com.carrefour.kata.domain.account;

import com.carrefour.kata.domain.account.event.PointsEarned;
import com.carrefour.kata.domain.account.event.PointsExpired;
import com.carrefour.kata.domain.account.event.PointsExpiringSoon;
import com.carrefour.kata.domain.account.event.PointsSpent;
import com.carrefour.kata.domain.account.exception.InsufficientPointsException;
import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.account.vo.LotId;
import com.carrefour.kata.domain.account.vo.SpendingType;
import com.carrefour.kata.domain.shared.DomainEvent;
import com.carrefour.kata.domain.shared.Points;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Racine d'agrégat : le compte de fidélité d'un client.
 *
 * <p>Propriétaire de l'invariant FIFO sur ses {@link PointsLot}s : les points gagnés
 * en premier sont dépensés en premier et expirent en premier. Les lots ne sont jamais
 * exposés à une mutation en dehors de l'agrégat.</p>
 */
public final class LoyaltyAccount {

    private final CustomerId customerId;
    private final List<PointsLot> lots;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private LoyaltyAccount(CustomerId customerId, List<PointsLot> lots) {
        this.customerId = Objects.requireNonNull(customerId, "customerId");
        this.lots = new ArrayList<>(lots);
        this.lots.sort(Comparator.comparing(PointsLot::earnedAt));
    }

    public static LoyaltyAccount openFor(CustomerId customerId) {
        return new LoyaltyAccount(customerId, List.of());
    }

    /**
     * Point d'entrée de reconstitution pour la couche de persistance.
     */
    public static LoyaltyAccount restore(CustomerId customerId, List<PointsLot> lots) {
        return new LoyaltyAccount(customerId, lots);
    }

    public void earn(Points points, LocalDate expiresAt) {
        earn(points, LocalDate.now(), expiresAt);
    }

    public void earn(Points points, LocalDate earnedAt, LocalDate expiresAt) {
        if (points.isZero()) {
            throw new IllegalArgumentException("Cannot earn zero points");
        }
        PointsLot lot = new PointsLot(LotId.newId(), points, earnedAt, expiresAt);
        lots.add(lot);
        lots.sort(Comparator.comparing(PointsLot::earnedAt));
        record(new PointsEarned(customerId, lot.id(), points, expiresAt, Instant.now()));
    }

    public void spend(Points points, SpendingType type) {
        spend(points, type, LocalDate.now());
    }

    /**
     * Consomme des points en FIFO sur les lots non expirés.
     *
     * @throws InsufficientPointsException si le solde disponible est inférieur
     *                                     au montant demandé
     */
    public void spend(Points points, SpendingType type, LocalDate today) {
        if (points.isZero()) {
            throw new IllegalArgumentException("Cannot spend zero points");
        }
        Points available = balance(today);
        if (points.isGreaterThan(available)) {
            throw new InsufficientPointsException(customerId, points, available);
        }
        Points remainingToSpend = points;
        for (PointsLot lot : lots) {
            if (remainingToSpend.isZero()) {
                break;
            }
            if (lot.isExpired(today) || lot.isEmpty()) {
                continue;
            }
            Points taken = lot.consume(remainingToSpend);
            remainingToSpend = remainingToSpend.subtract(taken);
        }
        lots.removeIf(PointsLot::isEmpty);
        record(new PointsSpent(customerId, points, type, Instant.now()));
    }

    /**
     * Purge les lots expirés ; enregistre un événement {@link PointsExpired} si
     * des points ont été perdus.
     */
    public void expireLots(LocalDate today) {
        Points expired = lots.stream()
                .filter(lot -> lot.isExpired(today))
                .map(PointsLot::remaining)
                .reduce(Points.ZERO, Points::add);
        lots.removeIf(lot -> lot.isExpired(today));
        if (!expired.isZero()) {
            record(new PointsExpired(customerId, expired, Instant.now()));
        }
    }

    public Points balance() {
        return balance(LocalDate.now());
    }

    public Points balance(LocalDate today) {
        return lots.stream()
                .filter(lot -> !lot.isExpired(today))
                .map(PointsLot::remaining)
                .reduce(Points.ZERO, Points::add);
    }

    /**
     * Enregistre un événement {@link PointsExpiringSoon} pour le lot donné.
     * Appelé par {@code ExpirationNotificationService} pour maintenir l'enregistrement
     * des événements à l'intérieur de l'agrégat.
     */
    public void recordExpiringSoon(PointsLot lot, LocalDate today) {
        record(new PointsExpiringSoon(customerId, lot.remaining(), lot.expiresAt(), java.time.Instant.now()));
    }

    public List<PointsLot> lotsExpiringBefore(LocalDate date) {
        return lots.stream()
                .filter(lot -> lot.expiresAt().isBefore(date))
                .toList();
    }

    public List<PointsLot> lots() {
        return List.copyOf(lots);
    }

    public CustomerId customerId() {
        return customerId;
    }

    /**
     * Retourne les événements enregistrés et vide la liste interne.
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    private void record(DomainEvent event) {
        domainEvents.add(event);
    }
}
