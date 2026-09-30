package com.carrefour.kata.application.usecase;

import com.carrefour.kata.application.exception.AccountNotFoundException;
import com.carrefour.kata.domain.shared.ClockPort;
import com.carrefour.kata.application.usecase.getbalance.GetBalanceQuery;
import com.carrefour.kata.application.usecase.getbalance.GetBalanceResponse;
import com.carrefour.kata.application.usecase.getbalance.GetBalanceUseCase;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBalanceUseCaseTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 25);

    @Mock
    private LoyaltyAccountRepository accountRepository;

    @Mock
    private ClockPort clock;

    private GetBalanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetBalanceUseCase(accountRepository, clock);
    }

    @Test
    void get_balance_returns_correct_balance_and_lots() {
        when(clock.today()).thenReturn(TODAY);
        CustomerId customerId = CustomerId.of("customer-1");
        LoyaltyAccount account = LoyaltyAccount.openFor(customerId);
        LocalDate expires1 = TODAY.plusMonths(3);
        LocalDate expires2 = TODAY.plusMonths(6);
        account.earn(Points.of(100), TODAY, expires1);
        account.earn(Points.of(200), TODAY, expires2);
        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(account));

        GetBalanceResponse response = useCase.execute(new GetBalanceQuery("customer-1"));

        assertThat(response.customerId()).isEqualTo("customer-1");
        assertThat(response.balance()).isEqualTo(300);
        assertThat(response.lots()).hasSize(2);
    }

    @Test
    void get_balance_excludes_expired_lots() {
        when(clock.today()).thenReturn(TODAY);
        CustomerId customerId = CustomerId.of("customer-1");
        LoyaltyAccount account = LoyaltyAccount.openFor(customerId);
        LocalDate alreadyExpired = TODAY.minusDays(1);
        LocalDate future = TODAY.plusMonths(3);
        account.earn(Points.of(50), TODAY.minusMonths(2), alreadyExpired);
        account.earn(Points.of(150), TODAY, future);
        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(account));

        GetBalanceResponse response = useCase.execute(new GetBalanceQuery("customer-1"));

        assertThat(response.balance()).isEqualTo(150);
        assertThat(response.lots()).hasSize(1);
        assertThat(response.lots().get(0).remaining()).isEqualTo(150);
    }

    @Test
    void get_balance_on_missing_account_throws_exception() {
        when(accountRepository.findByCustomerId(CustomerId.of("unknown"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GetBalanceQuery("unknown")))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
