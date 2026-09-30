package com.carrefour.kata.application.usecase.notifyexpiring;

import com.carrefour.kata.application.exception.AccountNotFoundException;
import com.carrefour.kata.domain.account.ExpirationNotificationService;
import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.repository.LoyaltyAccountRepository;
import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.shared.ClockPort;
import com.carrefour.kata.domain.shared.NotificationPort;
import lombok.RequiredArgsConstructor;

/**
 * Use case : notifier le client des points expirant dans une fenêtre de temps donnée.
 */
@RequiredArgsConstructor
public class NotifyExpiringPointsUseCase {

    private final LoyaltyAccountRepository accountRepository;
    private final ExpirationNotificationService expirationNotificationService;
    private final NotificationPort notificationPort;
    private final ClockPort clock;

    public void execute(NotifyExpiringPointsCommand command) {
        CustomerId customerId = CustomerId.of(command.customerId());
        LoyaltyAccount account = accountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new AccountNotFoundException(command.customerId()));

        expirationNotificationService.notifyExpiringSoon(account, command.noticeDays(), clock.today(), notificationPort);
    }
}
