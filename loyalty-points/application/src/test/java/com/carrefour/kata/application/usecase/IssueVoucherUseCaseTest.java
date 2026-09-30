package com.carrefour.kata.application.usecase;

import com.carrefour.kata.application.exception.AccountNotFoundException;
import com.carrefour.kata.domain.shared.ClockPort;
import com.carrefour.kata.application.usecase.issuevoucher.IssueVoucherCommand;
import com.carrefour.kata.application.usecase.issuevoucher.IssueVoucherResponse;
import com.carrefour.kata.application.usecase.issuevoucher.IssueVoucherUseCase;
import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.exception.InsufficientPointsException;
import com.carrefour.kata.domain.account.repository.LoyaltyAccountRepository;
import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.shared.Points;
import com.carrefour.kata.domain.voucher.VoucherIssuanceService;
import com.carrefour.kata.domain.voucher.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueVoucherUseCaseTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 25);
    private static final LocalDate EXPIRES_AT = TODAY.plusMonths(6);

    @Mock
    private LoyaltyAccountRepository accountRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private ClockPort clock;

    private IssueVoucherUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new IssueVoucherUseCase(accountRepository, voucherRepository,
                new VoucherIssuanceService(), clock);
    }

    @Test
    void issue_voucher_successful_returns_correct_response() {
        when(clock.today()).thenReturn(TODAY);
        CustomerId customerId = CustomerId.of("customer-1");
        LoyaltyAccount account = LoyaltyAccount.openFor(customerId);
        account.earn(Points.of(500), TODAY, EXPIRES_AT);
        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(account));

        IssueVoucherResponse response = useCase.execute(new IssueVoucherCommand("customer-1", 200));

        assertThat(response.customerId()).isEqualTo("customer-1");
        assertThat(response.value()).isEqualTo(200);
        assertThat(response.issuedAt()).isEqualTo(TODAY);
        assertThat(response.expiresAt()).isEqualTo(TODAY.plusYears(1));
        assertThat(response.voucherId()).isNotBlank();

        verify(accountRepository).save(account);
        verify(voucherRepository).save(any());
    }

    @Test
    void issue_voucher_with_insufficient_balance_throws_exception() {
        when(clock.today()).thenReturn(TODAY);
        CustomerId customerId = CustomerId.of("customer-1");
        LoyaltyAccount account = LoyaltyAccount.openFor(customerId);
        account.earn(Points.of(50), TODAY, EXPIRES_AT);
        when(accountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() ->
                useCase.execute(new IssueVoucherCommand("customer-1", 200))
        ).isInstanceOf(InsufficientPointsException.class);
    }

    @Test
    void issue_voucher_on_missing_account_throws_account_not_found() {
        when(accountRepository.findByCustomerId(CustomerId.of("unknown"))).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.execute(new IssueVoucherCommand("unknown", 100))
        ).isInstanceOf(AccountNotFoundException.class);
    }
}
