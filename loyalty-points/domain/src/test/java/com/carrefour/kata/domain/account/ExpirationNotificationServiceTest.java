package com.carrefour.kata.domain.account;

import com.carrefour.kata.domain.account.event.PointsExpiringSoon;
import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.account.vo.SpendingType;
import com.carrefour.kata.domain.shared.DomainEvent;
import com.carrefour.kata.domain.shared.NotificationPort;
import com.carrefour.kata.domain.shared.Points;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExpirationNotificationServiceTest {

    private static final CustomerId CUSTOMER = CustomerId.of("customer-1");
    private static final LocalDate TODAY = LocalDate.of(2026, 7, 25);
    private static final int NOTICE_DAYS = 7;

    private ExpirationNotificationService service;

    @BeforeEach
    void setUp() {
        service = new ExpirationNotificationService();
    }

    private LoyaltyAccount emptyAccount() {
        LoyaltyAccount account = LoyaltyAccount.openFor(CUSTOMER);
        account.pullDomainEvents();
        return account;
    }

    @Test
    @DisplayName("lots expiring within the notice window produce PointsExpiringSoon events")
    void notifyExpiringSoon_lotsInWindow_recordsEvents() {
        LoyaltyAccount account = emptyAccount();
        // expires in 3 days — within the 7-day window
        account.earn(Points.of(100), TODAY.minusMonths(6), TODAY.plusDays(3));
        // expires in 5 days — also within the 7-day window
        account.earn(Points.of(50), TODAY.minusMonths(3), TODAY.plusDays(5));
        account.pullDomainEvents();

        List<String> notified = new ArrayList<>();
        NotificationPort notificationPort = (customerId, points, expiresAt) -> notified.add(customerId.value());
        service.notifyExpiringSoon(account, NOTICE_DAYS, TODAY, notificationPort);

        assertThat(notified).hasSize(2);
        List<DomainEvent> events = account.pullDomainEvents();
        assertThat(events).hasSize(2);
        assertThat(events).allSatisfy(e -> assertThat(e).isInstanceOf(PointsExpiringSoon.class));
    }

    @Test
    @DisplayName("PointsExpiringSoon event carries the correct points amount and expiry date")
    void notifyExpiringSoon_eventContainsCorrectData() {
        LoyaltyAccount account = emptyAccount();
        LocalDate expiresAt = TODAY.plusDays(3);
        account.earn(Points.of(75), TODAY.minusMonths(6), expiresAt);
        account.pullDomainEvents();

        NotificationPort notificationPort = (cid, pts, exp) -> {};
        service.notifyExpiringSoon(account, NOTICE_DAYS, TODAY, notificationPort);

        List<DomainEvent> events = account.pullDomainEvents();
        assertThat(events).singleElement()
                .isInstanceOfSatisfying(PointsExpiringSoon.class, event -> {
                    assertThat(event.customerId()).isEqualTo(CUSTOMER);
                    assertThat(event.points()).isEqualTo(Points.of(75));
                    assertThat(event.expiresAt()).isEqualTo(expiresAt);
                    assertThat(event.occurredOn()).isNotNull();
                });
    }

    @Test
    @DisplayName("lots expiring outside the notice window produce no events")
    void notifyExpiringSoon_lotsOutsideWindow_noEvents() {
        LoyaltyAccount account = emptyAccount();
        // expires in 10 days — beyond the 7-day window
        account.earn(Points.of(100), TODAY.minusMonths(1), TODAY.plusDays(10));
        account.pullDomainEvents();

        NotificationPort notificationPort = (customerId, points, expiresAt) -> {};
        service.notifyExpiringSoon(account, NOTICE_DAYS, TODAY, notificationPort);

        assertThat(account.pullDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("empty lots in the window are ignored and produce no events")
    void notifyExpiringSoon_emptyLotsIgnored() {
        LoyaltyAccount account = emptyAccount();
        account.earn(Points.of(30), TODAY.minusMonths(6), TODAY.plusDays(3));
        // fully consume that lot so it is empty
        account.spend(Points.of(30), SpendingType.PAYMENT, TODAY);
        account.pullDomainEvents();

        NotificationPort notificationPort = (customerId, points, expiresAt) -> {};
        service.notifyExpiringSoon(account, NOTICE_DAYS, TODAY, notificationPort);

        // the empty lot was removed during spend, so no events expected
        assertThat(account.pullDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("mixed lots: only those inside the window and non-empty trigger events")
    void notifyExpiringSoon_mixedLots_onlyEligibleLotProducesEvent() {
        LoyaltyAccount account = emptyAccount();
        // inside window
        account.earn(Points.of(40), TODAY.minusMonths(6), TODAY.plusDays(4));
        // outside window
        account.earn(Points.of(60), TODAY.minusMonths(1), TODAY.plusDays(14));
        account.pullDomainEvents();

        NotificationPort notificationPort = (customerId, points, expiresAt) -> {};
        service.notifyExpiringSoon(account, NOTICE_DAYS, TODAY, notificationPort);

        List<DomainEvent> events = account.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOfSatisfying(PointsExpiringSoon.class,
                event -> assertThat(event.points()).isEqualTo(Points.of(40)));
    }
}
