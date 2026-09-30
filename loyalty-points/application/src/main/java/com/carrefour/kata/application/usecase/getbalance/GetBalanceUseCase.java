package com.carrefour.kata.application.usecase.getbalance;

import com.carrefour.kata.application.exception.AccountNotFoundException;
import com.carrefour.kata.domain.shared.ClockPort;
import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.repository.LoyaltyAccountRepository;
import com.carrefour.kata.domain.account.vo.CustomerId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case : récupérer le solde et les lots actifs d'un compte de fidélité.
 */
@RequiredArgsConstructor
public class GetBalanceUseCase {

    private final LoyaltyAccountRepository accountRepository;
    private final ClockPort clock;

    @Transactional(readOnly = true)
    public GetBalanceResponse execute(GetBalanceQuery query) {
        CustomerId customerId = CustomerId.of(query.customerId());
        LoyaltyAccount account = accountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new AccountNotFoundException(query.customerId()));

        var today = clock.today();
        List<GetBalanceResponse.LotSummary> lots = account.lots().stream()
                .filter(lot -> !lot.isExpired(today))
                .map(lot -> new GetBalanceResponse.LotSummary(lot.remaining().value(), lot.expiresAt()))
                .toList();

        return new GetBalanceResponse(
                query.customerId(),
                account.balance(today).value(),
                lots
        );
    }
}
