package com.carrefour.kata.application.usecase.issuevoucher;

import com.carrefour.kata.application.exception.AccountNotFoundException;
import com.carrefour.kata.domain.shared.ClockPort;
import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.repository.LoyaltyAccountRepository;
import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.shared.Points;
import com.carrefour.kata.domain.voucher.Voucher;
import com.carrefour.kata.domain.voucher.VoucherIssuanceService;
import com.carrefour.kata.domain.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case : convertir des points de fidélité en bon d'achat.
 */
@RequiredArgsConstructor
public class IssueVoucherUseCase {

    private final LoyaltyAccountRepository accountRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherIssuanceService voucherIssuanceService;
    private final ClockPort clock;

    @Transactional
    public IssueVoucherResponse execute(IssueVoucherCommand command) {
        CustomerId customerId = CustomerId.of(command.customerId());
        LoyaltyAccount account = accountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new AccountNotFoundException(command.customerId()));

        Points points = Points.of(command.points());
        Voucher voucher = voucherIssuanceService.issueVoucher(account, points, clock.today());

        accountRepository.save(account);
        voucherRepository.save(voucher);

        return new IssueVoucherResponse(
                voucher.id().value(),
                voucher.customerId().value(),
                voucher.value().value(),
                voucher.issuedAt(),
                voucher.expiresAt()
        );
    }
}
