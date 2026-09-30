package com.carrefour.kata.application.usecase;

import com.carrefour.kata.domain.shared.ClockPort;
import com.carrefour.kata.application.usecase.earnpoints.EarnPointsCommand;
import com.carrefour.kata.application.usecase.earnpoints.EarnPointsResponse;
import com.carrefour.kata.application.usecase.earnpoints.EarnPointsUseCase;
import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.repository.LoyaltyAccountRepository;
import com.carrefour.kata.domain.account.vo.CustomerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EarnPointsUseCaseTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 25);
    private static final LocalDate EXPIRES_AT = TODAY.plusMonths(6);

    @Mock
    private LoyaltyAccountRepository accountRepository;

    @Mock
    private ClockPort clock;

    private EarnPointsUseCase useCase;

    @BeforeEach
    void setUp() {
        when(clock.today()).thenReturn(TODAY);
        useCase = new EarnPointsUseCase(accountRepository, clock);
    }

    @Test
    void earn_on_existing_account_increases_balance() {
        CustomerId customerId = CustomerId.of("customer-1");
        LoyaltyAccount existing = LoyaltyAccount.openFor(customerId);
        existing.earn(com.carrefour.kata.domain.shared.Points.of(100), TODAY, EXPIRES_AT);
        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(existing));

        EarnPointsResponse response = useCase.execute(new EarnPointsCommand("customer-1", 50, EXPIRES_AT));

        assertThat(response.customerId()).isEqualTo("customer-1");
        assertThat(response.earnedPoints()).isEqualTo(50);
        assertThat(response.newBalance()).isEqualTo(150);
        assertThat(response.expiresAt()).isEqualTo(EXPIRES_AT);
        verify(accountRepository).save(existing);
    }

    @Test
    void earn_on_nonexistent_account_creates_new_account() {
        CustomerId customerId = CustomerId.of("new-customer");
        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());

        EarnPointsResponse response = useCase.execute(new EarnPointsCommand("new-customer", 200, EXPIRES_AT));

        assertThat(response.customerId()).isEqualTo("new-customer");
        assertThat(response.earnedPoints()).isEqualTo(200);
        assertThat(response.newBalance()).isEqualTo(200);

        ArgumentCaptor<LoyaltyAccount> captor = ArgumentCaptor.forClass(LoyaltyAccount.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().customerId()).isEqualTo(customerId);
    }
}
