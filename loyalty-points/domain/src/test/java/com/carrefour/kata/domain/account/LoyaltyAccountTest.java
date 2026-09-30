package com.carrefour.kata.domain.account;

import com.carrefour.kata.domain.account.event.PointsEarned;
import com.carrefour.kata.domain.account.event.PointsExpired;
import com.carrefour.kata.domain.account.event.PointsSpent;
import com.carrefour.kata.domain.account.exception.InsufficientPointsException;
import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.account.vo.SpendingType;
import com.carrefour.kata.domain.shared.DomainEvent;
import com.carrefour.kata.domain.shared.Points;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LoyaltyAccountTest {

    private static final CustomerId CUSTOMER = CustomerId.of("customer-1");
    private static final LocalDate TODAY = LocalDate.of(2026, 7, 25);

    private LoyaltyAccount emptyAccount() {
        LoyaltyAccount account = LoyaltyAccount.openFor(CUSTOMER);
        account.pullDomainEvents();
        return account;
    }

    @Nested
    class Earning {

        @Test
        @DisplayName("earning points increases the balance")
        void earnIncreasesBalance() {
            LoyaltyAccount account = emptyAccount();

            account.earn(Points.of(100), TODAY, TODAY.plusMonths(6));

            assertThat(account.balance(TODAY)).isEqualTo(Points.of(100));
        }

        @Test
        @DisplayName("earning records a PointsEarned event")
        void earnRecordsEvent() {
            LoyaltyAccount account = emptyAccount();

            account.earn(Points.of(100), TODAY, TODAY.plusMonths(6));

            List<DomainEvent> events = account.pullDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOfSatisfying(PointsEarned.class, event -> {
                assertThat(event.customerId()).isEqualTo(CUSTOMER);
                assertThat(event.points()).isEqualTo(Points.of(100));
                assertThat(event.expiresAt()).isEqualTo(TODAY.plusMonths(6));
                assertThat(event.occurredOn()).isNotNull();
            });
        }
    }

    @Nested
    class Spending {

        @Test
        @DisplayName("spending consumes the oldest lot first (FIFO)")
        void spendsFifoAcrossLots() {
            LoyaltyAccount account = emptyAccount();
            account.earn(Points.of(50), TODAY.minusDays(10), TODAY.plusMonths(1));
            account.earn(Points.of(30), TODAY.minusDays(5), TODAY.plusMonths(2));

            account.spend(Points.of(60), SpendingType.PAYMENT, TODAY);

            // 50 taken from the oldest lot (emptied), 10 from the second
            assertThat(account.balance(TODAY)).isEqualTo(Points.of(20));
            assertThat(account.lots()).hasSize(1);
            assertThat(account.lots().getFirst().remaining()).isEqualTo(Points.of(20));
            assertThat(account.lots().getFirst().expiresAt()).isEqualTo(TODAY.plusMonths(2));
        }

        @Test
        @DisplayName("a partially consumed lot keeps its remainder")
        void partialConsumptionOfOldestLot() {
            LoyaltyAccount account = emptyAccount();
            account.earn(Points.of(50), TODAY.minusDays(10), TODAY.plusMonths(1));
            account.earn(Points.of(30), TODAY.minusDays(5), TODAY.plusMonths(2));

            account.spend(Points.of(20), SpendingType.DONATION, TODAY);

            assertThat(account.balance(TODAY)).isEqualTo(Points.of(60));
            assertThat(account.lots().getFirst().remaining()).isEqualTo(Points.of(30));
        }

        @ParameterizedTest(name = "balance={0}, overspend={1} → InsufficientPointsException")
        @CsvSource({"40,41", "10,11", "1,2", "0,1"})
        @DisplayName("spending more than the balance is rejected")
        void insufficientBalanceIsRejected(int balance, int overspend) {
            LoyaltyAccount account = emptyAccount();
            if (balance > 0) {
                account.earn(Points.of(balance), TODAY, TODAY.plusMonths(1));
            }

            assertThatExceptionOfType(InsufficientPointsException.class)
                    .isThrownBy(() -> account.spend(Points.of(overspend), SpendingType.PAYMENT, TODAY));
            assertThat(account.balance(TODAY)).isEqualTo(Points.of(balance));
        }

        @Test
        @DisplayName("expired lots cannot be spent")
        void expiredLotsAreNotSpendable() {
            LoyaltyAccount account = emptyAccount();
            account.earn(Points.of(100), TODAY.minusMonths(7), TODAY.minusDays(1));
            account.earn(Points.of(10), TODAY.minusDays(5), TODAY.plusMonths(2));

            assertThatExceptionOfType(InsufficientPointsException.class)
                    .isThrownBy(() -> account.spend(Points.of(50), SpendingType.VOUCHER, TODAY));
        }

        @ParameterizedTest(name = "spend type {0} records a PointsSpent event")
        @EnumSource(SpendingType.class)
        @DisplayName("spending records a PointsSpent event with its type")
        void spendRecordsEvent(SpendingType type) {
            LoyaltyAccount account = emptyAccount();
            account.earn(Points.of(100), TODAY, TODAY.plusMonths(6));
            account.pullDomainEvents();

            account.spend(Points.of(25), type, TODAY);

            assertThat(account.pullDomainEvents())
                    .singleElement()
                    .isInstanceOfSatisfying(PointsSpent.class, event -> {
                        assertThat(event.points()).isEqualTo(Points.of(25));
                        assertThat(event.spendingType()).isEqualTo(type);
                    });
        }
    }

    @Nested
    class Expiration {

        @Test
        @DisplayName("expired lots are excluded from the balance")
        void balanceIgnoresExpiredLots() {
            LoyaltyAccount account = emptyAccount();
            account.earn(Points.of(100), TODAY.minusMonths(7), TODAY.minusDays(1));
            account.earn(Points.of(30), TODAY.minusDays(5), TODAY.plusMonths(2));

            assertThat(account.balance(TODAY)).isEqualTo(Points.of(30));
        }

        @Test
        @DisplayName("a lot is still valid on its expiry date")
        void lotValidOnExpiryDate() {
            LoyaltyAccount account = emptyAccount();
            account.earn(Points.of(10), TODAY.minusMonths(1), TODAY);

            assertThat(account.balance(TODAY)).isEqualTo(Points.of(10));
        }

        @Test
        @DisplayName("expireLots purges expired lots and records PointsExpired")
        void expireLotsPurgesAndRecordsEvent() {
            LoyaltyAccount account = emptyAccount();
            account.earn(Points.of(100), TODAY.minusMonths(7), TODAY.minusDays(1));
            account.earn(Points.of(30), TODAY.minusDays(5), TODAY.plusMonths(2));
            account.pullDomainEvents();

            account.expireLots(TODAY);

            assertThat(account.lots()).hasSize(1);
            assertThat(account.balance(TODAY)).isEqualTo(Points.of(30));
            assertThat(account.pullDomainEvents())
                    .singleElement()
                    .isInstanceOfSatisfying(PointsExpired.class, event ->
                            assertThat(event.points()).isEqualTo(Points.of(100)));
        }

        @Test
        @DisplayName("expireLots records nothing when no lot is expired")
        void expireLotsWithoutExpiredLotIsSilent() {
            LoyaltyAccount account = emptyAccount();
            account.earn(Points.of(30), TODAY, TODAY.plusMonths(2));
            account.pullDomainEvents();

            account.expireLots(TODAY);

            assertThat(account.pullDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("lotsExpiringBefore returns lots to notify")
        void lotsExpiringBeforeSelectsUpcomingExpirations() {
            LoyaltyAccount account = emptyAccount();
            account.earn(Points.of(10), TODAY.minusMonths(6), TODAY.plusDays(3));
            account.earn(Points.of(20), TODAY.minusMonths(1), TODAY.plusMonths(5));

            List<PointsLot> expiring = account.lotsExpiringBefore(TODAY.plusDays(7));

            assertThat(expiring).hasSize(1);
            assertThat(expiring.getFirst().remaining()).isEqualTo(Points.of(10));
        }
    }

    @Nested
    class Events {

        @Test
        @DisplayName("pullDomainEvents returns then clears the events")
        void pullReturnsAndClears() {
            LoyaltyAccount account = emptyAccount();
            account.earn(Points.of(10), TODAY, TODAY.plusMonths(1));
            account.spend(Points.of(5), SpendingType.PAYMENT, TODAY);

            assertThat(account.pullDomainEvents()).hasSize(2);
            assertThat(account.pullDomainEvents()).isEmpty();
        }
    }

    @Test
    @DisplayName("the lots list is not exposed for mutation")
    void lotsListIsImmutable() {
        LoyaltyAccount account = emptyAccount();
        account.earn(Points.of(10), TODAY, TODAY.plusMonths(1));

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> account.lots().clear());
    }
}
