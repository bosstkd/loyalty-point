package com.carrefour.kata.domain.voucher;

import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.event.PointsSpent;
import com.carrefour.kata.domain.account.exception.InsufficientPointsException;
import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.account.vo.SpendingType;
import com.carrefour.kata.domain.shared.DomainEvent;
import com.carrefour.kata.domain.shared.Points;
import com.carrefour.kata.domain.voucher.vo.VoucherStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class VoucherIssuanceServiceTest {

    private static final CustomerId CUSTOMER = CustomerId.of("customer-1");
    private static final LocalDate TODAY = LocalDate.of(2026, 7, 25);

    private VoucherIssuanceService service;

    @BeforeEach
    void setUp() {
        service = new VoucherIssuanceService();
    }

    private LoyaltyAccount accountWith(int points) {
        LoyaltyAccount account = LoyaltyAccount.openFor(CUSTOMER);
        account.earn(Points.of(points), TODAY.minusDays(1), TODAY.plusMonths(6));
        account.pullDomainEvents();
        return account;
    }

    @Test
    @DisplayName("successful issuance returns an ISSUED voucher with correct value and owner")
    void issueVoucher_success_returnsIssuedVoucher() {
        LoyaltyAccount account = accountWith(500);

        Voucher voucher = service.issueVoucher(account, Points.of(200), TODAY);

        assertThat(voucher).isNotNull();
        assertThat(voucher.status()).isEqualTo(VoucherStatus.ISSUED);
        assertThat(voucher.customerId()).isEqualTo(CUSTOMER);
        assertThat(voucher.value()).isEqualTo(Points.of(200));
        assertThat(voucher.issuedAt()).isEqualTo(TODAY);
    }

    @Test
    @DisplayName("voucher validity is exactly today + 1 year")
    void issueVoucher_validityIsOneYear() {
        LoyaltyAccount account = accountWith(500);

        Voucher voucher = service.issueVoucher(account, Points.of(100), TODAY);

        assertThat(voucher.expiresAt()).isEqualTo(TODAY.plusYears(1));
    }

    @Test
    @DisplayName("successful issuance debits the account balance")
    void issueVoucher_success_debitsBalance() {
        LoyaltyAccount account = accountWith(500);

        service.issueVoucher(account, Points.of(200), TODAY);

        assertThat(account.balance(TODAY)).isEqualTo(Points.of(300));
    }

    @Test
    @DisplayName("successful issuance records a PointsSpent(VOUCHER) event on the account")
    void issueVoucher_success_recordsPointsSpentEvent() {
        LoyaltyAccount account = accountWith(500);

        service.issueVoucher(account, Points.of(200), TODAY);

        List<DomainEvent> events = account.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOfSatisfying(PointsSpent.class, event -> {
            assertThat(event.customerId()).isEqualTo(CUSTOMER);
            assertThat(event.points()).isEqualTo(Points.of(200));
            assertThat(event.spendingType()).isEqualTo(SpendingType.VOUCHER);
        });
    }

    @Test
    @DisplayName("issuance with insufficient balance throws InsufficientPointsException")
    void issueVoucher_insufficientBalance_throws() {
        LoyaltyAccount account = accountWith(50);

        assertThatExceptionOfType(InsufficientPointsException.class)
                .isThrownBy(() -> service.issueVoucher(account, Points.of(100), TODAY));
    }

    @Test
    @DisplayName("failed issuance leaves the account balance unchanged")
    void issueVoucher_insufficientBalance_doesNotDebitAccount() {
        LoyaltyAccount account = accountWith(50);

        try {
            service.issueVoucher(account, Points.of(100), TODAY);
        } catch (InsufficientPointsException ignored) {
            // expected
        }

        assertThat(account.balance(TODAY)).isEqualTo(Points.of(50));
    }
}
