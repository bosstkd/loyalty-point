package com.carrefour.kata.domain.shared;

/**
 * Quantité de points de fidélité. Immuable, jamais négative.
 */
public record Points(int value) {

    public static final Points ZERO = new Points(0);

    public Points {
        if (value < 0) {
            throw new IllegalArgumentException("Points cannot be negative: " + value);
        }
    }

    public static Points of(int value) {
        return new Points(value);
    }

    public Points add(Points other) {
        return new Points(this.value + other.value);
    }

    public Points subtract(Points other) {
        if (other.value > this.value) {
            throw new IllegalArgumentException(
                    "Cannot subtract %d points from %d".formatted(other.value, this.value));
        }
        return new Points(this.value - other.value);
    }

    public boolean isZero() {
        return value == 0;
    }

    public boolean isGreaterThan(Points other) {
        return this.value > other.value;
    }
}
