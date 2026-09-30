package com.carrefour.kata.domain.account.event;

import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.shared.DomainEvent;
import com.carrefour.kata.domain.shared.Points;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Le domaine constate le fait ; l'infrastructure le transforme en
 * notification client.
 */
public record PointsExpiringSoon(
        CustomerId customerId,
        Points points,
        LocalDate expiresAt,
        Instant occurredOn) implements DomainEvent {
}
