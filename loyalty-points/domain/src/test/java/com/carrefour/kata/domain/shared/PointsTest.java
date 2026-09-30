package com.carrefour.kata.domain.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PointsTest {

    @ParameterizedTest(name = "Points.of({0}) throws IllegalArgumentException")
    @ValueSource(ints = {-1, -100, Integer.MIN_VALUE})
    @DisplayName("points cannot be negative")
    void rejectsNegativeValue(int value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Points.of(value))
                .withMessageContaining("negative");
    }

    @ParameterizedTest(name = "{0} + {1} = {2}")
    @CsvSource({"10,5,15", "0,5,5", "100,0,100", "1,999,1000"})
    @DisplayName("add sums two amounts")
    void addsPoints(int a, int b, int expected) {
        assertThat(Points.of(a).add(Points.of(b))).isEqualTo(Points.of(expected));
    }

    @ParameterizedTest(name = "{0} - {1} = {2}")
    @CsvSource({"10,4,6", "10,10,0", "100,1,99"})
    @DisplayName("subtract returns the difference")
    void subtractsPoints(int a, int b, int expected) {
        assertThat(Points.of(a).subtract(Points.of(b))).isEqualTo(Points.of(expected));
    }

    @Test
    @DisplayName("subtract refuses a negative result")
    void subtractCannotGoNegative() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Points.of(3).subtract(Points.of(5)));
    }

    @Test
    @DisplayName("ZERO is zero, others are not")
    void zeroDetection() {
        assertThat(Points.ZERO.isZero()).isTrue();
        assertThat(Points.of(1).isZero()).isFalse();
    }

    @Test
    @DisplayName("points of equal value are equal")
    void valueEquality() {
        assertThat(Points.of(7)).isEqualTo(Points.of(7));
    }

    @ParameterizedTest(name = "Points.of({0}).isGreaterThan(Points.of({1})) = {2}")
    @CsvSource({"8,7,true", "7,7,false", "6,7,false"})
    @DisplayName("isGreaterThan is strictly greater than, not ≥")
    void isGreaterThan(int left, int right, boolean expected) {
        assertThat(Points.of(left).isGreaterThan(Points.of(right))).isEqualTo(expected);
    }
}
