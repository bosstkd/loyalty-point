package com.carrefour.kata.application.usecase;

import com.carrefour.kata.application.exception.AccountNotFoundException;
import com.carrefour.kata.domain.shared.ClockPort;
import com.carrefour.kata.application.usecase.spendpoints.SpendPointsCommand;
import com.carrefour.kata.application.usecase.spendpoints.SpendPointsResponse;
import com.carrefour.kata.application.usecase.spendpoints.SpendPointsUseCase;
import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.exception.InsufficientPointsException;
import com.carrefour.kata.domain.account.repository.LoyaltyAccountRepository;
import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.account.vo.SpendingType;
import com.carrefour.kata.domain.shared.Points;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpendPointsUseCaseTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 25);
    private static final LocalDate EXPIRES_AT = TODAY.plusMonths(6);

    @Mock
    private LoyaltyAccountRepository accountRepository;

    @Mock
    private ClockPort clock;

    private SpendPointsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SpendPointsUseCase(accountRepository, clock);
    }

    @Test
    void spend_successful_reduces_balance() {
        when(clock.today()).thenReturn(TODAY);
        CustomerId customerId = CustomerId.of("customer-1");
        LoyaltyAccount account = LoyaltyAccount.openFor(customerId);
        account.earn(Points.of(300), TODAY, EXPIRES_AT);
        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(account));

        SpendPointsResponse response = useCase.execute(
                new SpendPointsCommand("customer-1", 100, SpendingType.PAYMENT));

        assertThat(response.customerId()).isEqualTo("customer-1");
        assertThat(response.spentPoints()).isEqualTo(100);
        assertThat(response.remainingBalance()).isEqualTo(200);
        verify(accountRepository).save(account);
    }

    @Test
    void spend_with_insufficient_balance_throws_domain_exception() {
        when(clock.today()).thenReturn(TODAY);
        CustomerId customerId = CustomerId.of("customer-1");
        LoyaltyAccount account = LoyaltyAccount.openFor(customerId);
        account.earn(Points.of(50), TODAY, EXPIRES_AT);
        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() ->
                useCase.execute(new SpendPointsCommand("customer-1", 100, SpendingType.PAYMENT))
        ).isInstanceOf(InsufficientPointsException.class);
    }

    @Test
    void spend_on_missing_account_throws_account_not_found() {
        when(accountRepository.findByCustomerId(CustomerId.of("unknown"))).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.execute(new SpendPointsCommand("unknown", 50, SpendingType.DONATION))
        ).isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("unknown");
    }
}
