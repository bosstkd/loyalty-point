package com.carrefour.kata.domain.shared;

/**
 * Classe de base pour toutes les violations de règles métier.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
