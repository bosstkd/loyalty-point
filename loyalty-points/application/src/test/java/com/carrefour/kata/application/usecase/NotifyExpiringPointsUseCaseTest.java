package com.carrefour.kata.application.usecase;

import com.carrefour.kata.domain.shared.ClockPort;
import com.carrefour.kata.domain.shared.NotificationPort;
import com.carrefour.kata.application.usecase.notifyexpiring.NotifyExpiringPointsCommand;
import com.carrefour.kata.application.usecase.notifyexpiring.NotifyExpiringPointsUseCase;
import com.carrefour.kata.domain.account.ExpirationNotificationService;
import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.repository.LoyaltyAccountRepository;
import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.shared.Points;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifyExpiringPointsUseCaseTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 25);

    @Mock
    private LoyaltyAccountRepository accountRepository;

    @Mock
    private NotificationPort notificationPort;

    @Mock
    private ClockPort clock;

    private NotifyExpiringPointsUseCase useCase;

    @BeforeEach
    void setUp() {
        when(clock.today()).thenReturn(TODAY);
        useCase = new NotifyExpiringPointsUseCase(accountRepository,
                new ExpirationNotificationService(), notificationPort, clock);
    }

    @Test
    void events_translated_to_notification_port_calls() {
        CustomerId customerId = CustomerId.of("customer-1");
        LoyaltyAccount account = LoyaltyAccount.openFor(customerId);
        LocalDate expiringSoon = TODAY.plusDays(5);
        account.earn(Points.of(100), TODAY, expiringSoon);
        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(account));

        useCase.execute(new NotifyExpiringPointsCommand("customer-1", 7));

        verify(notificationPort).notifyExpiringSoon(
                eq(customerId),
                eq(Points.of(100)),
                eq(expiringSoon)
        );
    }

    @Test
    void no_notification_when_no_lots_expiring_soon() {
        CustomerId customerId = CustomerId.of("customer-1");
        LoyaltyAccount account = LoyaltyAccount.openFor(customerId);
        account.earn(Points.of(100), TODAY, TODAY.plusMonths(3));
        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(account));

        useCase.execute(new NotifyExpiringPointsCommand("customer-1", 7));

        verifyNoInteractions(notificationPort);
    }
}
