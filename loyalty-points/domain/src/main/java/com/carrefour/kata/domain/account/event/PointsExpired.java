package com.carrefour.kata.domain.account.event;

import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.shared.DomainEvent;
import com.carrefour.kata.domain.shared.Points;

import java.time.Instant;

public record PointsExpired(
        CustomerId customerId,
        Points points,
        Instant occurredOn) implements DomainEvent {
}
