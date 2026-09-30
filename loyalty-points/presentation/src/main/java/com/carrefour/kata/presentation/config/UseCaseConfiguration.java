package com.carrefour.kata.presentation.config;

import com.carrefour.kata.application.usecase.earnpoints.EarnPointsUseCase;
import com.carrefour.kata.application.usecase.expirepoints.ExpirePointsUseCase;
import com.carrefour.kata.application.usecase.getbalance.GetBalanceUseCase;
import com.carrefour.kata.application.usecase.issuevoucher.IssueVoucherUseCase;
import com.carrefour.kata.application.usecase.notifyexpiring.NotifyExpiringPointsUseCase;
import com.carrefour.kata.application.usecase.spendpoints.SpendPointsUseCase;
import com.carrefour.kata.domain.account.ExpirationNotificationService;
import com.carrefour.kata.domain.account.repository.LoyaltyAccountRepository;
import com.carrefour.kata.domain.shared.ClockPort;
import com.carrefour.kata.domain.shared.NotificationPort;
import com.carrefour.kata.domain.voucher.VoucherIssuanceService;
import com.carrefour.kata.domain.voucher.repository.VoucherRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Déclare les use cases comme beans Spring (câblage manuel, composition root).
 * Les services de domaine sont auto-détectés via {@link DomainServiceConfiguration}.
 */
@Configuration
public class UseCaseConfiguration {

    @Bean
    public EarnPointsUseCase earnPointsUseCase(LoyaltyAccountRepository accountRepository, ClockPort clock) {
        return new EarnPointsUseCase(accountRepository, clock);
    }

    @Bean
    public SpendPointsUseCase spendPointsUseCase(LoyaltyAccountRepository accountRepository, ClockPort clock) {
        return new SpendPointsUseCase(accountRepository, clock);
    }

    @Bean
    public IssueVoucherUseCase issueVoucherUseCase(LoyaltyAccountRepository accountRepository,
                                                    VoucherRepository voucherRepository,
                                                    VoucherIssuanceService voucherIssuanceService,
                                                    ClockPort clock) {
        return new IssueVoucherUseCase(accountRepository, voucherRepository, voucherIssuanceService, clock);
    }

    @Bean
    public GetBalanceUseCase getBalanceUseCase(LoyaltyAccountRepository accountRepository, ClockPort clock) {
        return new GetBalanceUseCase(accountRepository, clock);
    }

    @Bean
    public ExpirePointsUseCase expirePointsUseCase(LoyaltyAccountRepository accountRepository, ClockPort clock) {
        return new ExpirePointsUseCase(accountRepository, clock);
    }

    @Bean
    public NotifyExpiringPointsUseCase notifyExpiringPointsUseCase(LoyaltyAccountRepository accountRepository,
                                                                     ExpirationNotificationService expirationNotificationService,
                                                                     NotificationPort notificationPort,
                                                                     ClockPort clock) {
        return new NotifyExpiringPointsUseCase(accountRepository, expirationNotificationService, notificationPort, clock);
    }
}
