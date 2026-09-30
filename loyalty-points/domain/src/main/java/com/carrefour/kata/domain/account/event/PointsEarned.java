package com.carrefour.kata.domain.account.event;

import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.account.vo.LotId;
import com.carrefour.kata.domain.account.vo.SpendingType;
import com.carrefour.kata.domain.shared.DomainEvent;
import com.carrefour.kata.domain.shared.Points;

import java.time.Instant;
import java.time.LocalDate;

public record PointsEarned(
        CustomerId customerId,
        LotId lotId,
        Points points,
        LocalDate expiresAt,
        Instant occurredOn) implements DomainEvent {
}
