package com.carrefour.kata.application.usecase.expirepoints;

import com.carrefour.kata.application.exception.AccountNotFoundException;
import com.carrefour.kata.domain.shared.ClockPort;
import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.repository.LoyaltyAccountRepository;
import com.carrefour.kata.domain.account.vo.CustomerId;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case : purger les lots expirés d'un compte de fidélité.
 */
@RequiredArgsConstructor
public class ExpirePointsUseCase {

    private final LoyaltyAccountRepository accountRepository;
    private final ClockPort clock;

    @Transactional
    public void execute(ExpirePointsCommand command) {
        CustomerId customerId = CustomerId.of(command.customerId());
        LoyaltyAccount account = accountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new AccountNotFoundException(command.customerId()));

        account.expireLots(clock.today());
        accountRepository.save(account);
    }
}
