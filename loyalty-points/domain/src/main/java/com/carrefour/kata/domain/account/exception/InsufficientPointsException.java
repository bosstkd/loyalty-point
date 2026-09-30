package com.carrefour.kata.domain.account.exception;

import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.shared.DomainException;
import com.carrefour.kata.domain.shared.Points;

/**
 * Levée lorsqu'une dépense dépasse le solde disponible (non expiré).
 */
public class InsufficientPointsException extends DomainException {

    public InsufficientPointsException(CustomerId customerId, Points requested, Points available) {
        super("Customer %s has %d points, cannot spend %d"
                .formatted(customerId.value(), available.value(), requested.value()));
    }
}
