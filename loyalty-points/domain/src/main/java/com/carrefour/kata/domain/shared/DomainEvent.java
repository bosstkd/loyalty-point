package com.carrefour.kata.domain.shared;

import java.time.Instant;

/**
 * Un fait survenu dans le domaine. Les événements sont des records immuables.
 */
public interface DomainEvent {

    Instant occurredOn();
}
