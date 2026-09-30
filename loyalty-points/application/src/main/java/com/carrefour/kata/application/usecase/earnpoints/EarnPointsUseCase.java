package com.carrefour.kata.application.usecase.earnpoints;

import com.carrefour.kata.domain.shared.ClockPort;
import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.repository.LoyaltyAccountRepository;
import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.shared.Points;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case : créditer des points sur un compte de fidélité.
 * Crée le compte s'il n'existe pas encore.
 */
@RequiredArgsConstructor
public class EarnPointsUseCase {

    private final LoyaltyAccountRepository accountRepository;
    private final ClockPort clock;

    @Transactional
    public EarnPointsResponse execute(EarnPointsCommand command) {
        CustomerId customerId = CustomerId.of(command.customerId());
        LoyaltyAccount account = accountRepository.findByCustomerId(customerId)
                .orElseGet(() -> LoyaltyAccount.openFor(customerId));

        Points points = Points.of(command.points());
        account.earn(points, clock.today(), command.expiresAt());
        accountRepository.save(account);

        return new EarnPointsResponse(
                command.customerId(),
                points.value(),
                account.balance(clock.today()).value(),
                command.expiresAt()
        );
    }
}
