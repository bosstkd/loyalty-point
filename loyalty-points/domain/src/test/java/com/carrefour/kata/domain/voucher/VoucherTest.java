package com.carrefour.kata.domain.voucher;

import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.voucher.exception.VoucherNotUsableException;
import com.carrefour.kata.domain.voucher.vo.VoucherStatus;
import com.carrefour.kata.domain.shared.Points;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class VoucherTest {

    private static final CustomerId CUSTOMER = CustomerId.of("customer-1");
    private static final LocalDate TODAY = LocalDate.of(2026, 7, 25);

    // Voucher expires TODAY + 3 months
    private Voucher issuedVoucher() {
        return Voucher.issue(CUSTOMER, Points.of(500), TODAY, TODAY.plusMonths(3));
    }

    @Test
    @DisplayName("a new voucher is ISSUED with an identity and a value")
    void issueCreatesVoucher() {
        Voucher voucher = issuedVoucher();

        assertThat(voucher.status()).isEqualTo(VoucherStatus.ISSUED);
        assertThat(voucher.id()).isNotNull();
        assertThat(voucher.customerId()).isEqualTo(CUSTOMER);
        assertThat(voucher.value()).isEqualTo(Points.of(500));
        assertThat(voucher.isExpired(TODAY)).isFalse();
    }

    @Test
    @DisplayName("a voucher cannot be worth zero points")
    void zeroValueIsRejected() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Voucher.issue(CUSTOMER, Points.ZERO, TODAY, TODAY.plusMonths(3)));
    }

    @Test
    @DisplayName("using an issued voucher marks it USED")
    void useMarksVoucherUsed() {
        Voucher voucher = issuedVoucher();

        voucher.use();

        assertThat(voucher.status()).isEqualTo(VoucherStatus.USED);
    }

    // -------------------------------------------------------------------------
    // Invalid state transitions — grouped under one parameterized test
    // -------------------------------------------------------------------------

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(
                        "used voucher cannot be reused",
                        (Consumer<Voucher>) Voucher::use,
                        (Consumer<Voucher>) Voucher::use,
                        "USED"),
                Arguments.of(
                        "expired voucher cannot be used",
                        (Consumer<Voucher>) Voucher::expire,
                        (Consumer<Voucher>) Voucher::use,
                        "EXPIRED"),
                Arguments.of(
                        "used voucher cannot be expired",
                        (Consumer<Voucher>) Voucher::use,
                        (Consumer<Voucher>) Voucher::expire,
                        "USED")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTransitions")
    @DisplayName("invalid state transitions throw VoucherNotUsableException")
    void invalidStateTransition_throws(
            String scenario,
            Consumer<Voucher> setup,
            Consumer<Voucher> action,
            String expectedMessageFragment) {

        Voucher voucher = issuedVoucher();
        setup.accept(voucher);

        assertThatExceptionOfType(VoucherNotUsableException.class)
                .isThrownBy(() -> action.accept(voucher))
                .withMessageContaining(expectedMessageFragment);
    }

    // -------------------------------------------------------------------------
    // Expiry date boundary checks
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "isExpired at +{0}m+{1}d = {2}")
    @CsvSource({
            "0,  1, false",  // well before expiry
            "3,  0, false",  // exactly on expiry date: still valid
            "3,  1, true",   // one day past expiry date
            "6,  0, true"    // long after expiry
    })
    @DisplayName("the voucher knows whether its date has passed")
    void expiryDateCheck(int plusMonths, int plusDays, boolean expectedExpired) {
        Voucher voucher = issuedVoucher(); // expires at TODAY + 3 months
        LocalDate checkDate = TODAY.plusMonths(plusMonths).plusDays(plusDays);

        assertThat(voucher.isExpired(checkDate)).isEqualTo(expectedExpired);
    }
}
