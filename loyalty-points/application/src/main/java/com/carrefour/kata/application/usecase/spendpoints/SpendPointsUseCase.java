package com.carrefour.kata.application.usecase.spendpoints;

import com.carrefour.kata.application.exception.AccountNotFoundException;
import com.carrefour.kata.domain.shared.ClockPort;
import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.repository.LoyaltyAccountRepository;
import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.shared.Points;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case : dépenser des points depuis un compte de fidélité (PAYMENT ou DONATION).
 */
@RequiredArgsConstructor
public class SpendPointsUseCase {

    private final LoyaltyAccountRepository accountRepository;
    private final ClockPort clock;

    @Transactional
    public SpendPointsResponse execute(SpendPointsCommand command) {
        CustomerId customerId = CustomerId.of(command.customerId());
        LoyaltyAccount account = accountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new AccountNotFoundException(command.customerId()));

        Points points = Points.of(command.points());
        account.spend(points, command.type(), clock.today());
        accountRepository.save(account);

        return new SpendPointsResponse(
                command.customerId(),
                points.value(),
                account.balance(clock.today()).value()
        );
    }
}
