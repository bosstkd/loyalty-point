package com.carrefour.kata.application.usecase;

import com.carrefour.kata.domain.shared.ClockPort;
import com.carrefour.kata.application.usecase.expirepoints.ExpirePointsCommand;
import com.carrefour.kata.application.usecase.expirepoints.ExpirePointsUseCase;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpirePointsUseCaseTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 25);

    @Mock
    private LoyaltyAccountRepository accountRepository;

    @Mock
    private ClockPort clock;

    private ExpirePointsUseCase useCase;

    @BeforeEach
    void setUp() {
        when(clock.today()).thenReturn(TODAY);
        useCase = new ExpirePointsUseCase(accountRepository, clock);
    }

    @Test
    void expire_lots_purges_expired_and_saves() {
        CustomerId customerId = CustomerId.of("customer-1");
        LoyaltyAccount account = LoyaltyAccount.openFor(customerId);
        // expired yesterday
        account.earn(Points.of(100), TODAY.minusMonths(2), TODAY.minusDays(1));
        // still valid
        account.earn(Points.of(200), TODAY, TODAY.plusMonths(3));
        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(account));

        useCase.execute(new ExpirePointsCommand("customer-1"));

        assertThat(account.balance(TODAY).value()).isEqualTo(200);
        verify(accountRepository).save(account);
    }

    @Test
    void expire_lots_with_no_expired_lots_still_saves() {
        CustomerId customerId = CustomerId.of("customer-1");
        LoyaltyAccount account = LoyaltyAccount.openFor(customerId);
        account.earn(Points.of(100), TODAY, TODAY.plusMonths(3));
        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(account));

        useCase.execute(new ExpirePointsCommand("customer-1"));

        assertThat(account.balance(TODAY).value()).isEqualTo(100);
        verify(accountRepository).save(account);
    }
}
