package com.carrefour.kata.presentation.controller;

import com.carrefour.kata.application.usecase.earnpoints.EarnPointsCommand;
import com.carrefour.kata.application.usecase.earnpoints.EarnPointsUseCase;
import com.carrefour.kata.application.usecase.getbalance.GetBalanceQuery;
import com.carrefour.kata.application.usecase.getbalance.GetBalanceUseCase;
import com.carrefour.kata.application.usecase.issuevoucher.IssueVoucherCommand;
import com.carrefour.kata.application.usecase.issuevoucher.IssueVoucherUseCase;
import com.carrefour.kata.application.usecase.spendpoints.SpendPointsCommand;
import com.carrefour.kata.application.usecase.spendpoints.SpendPointsUseCase;
import com.carrefour.kata.domain.account.vo.SpendingType;
import com.carrefour.kata.presentation.dto.BalanceResponse;
import com.carrefour.kata.presentation.dto.EarnPointsRequest;
import com.carrefour.kata.presentation.dto.EarnPointsResponse;
import com.carrefour.kata.presentation.dto.IssueVoucherRequest;
import com.carrefour.kata.presentation.dto.SpendPointsRequest;
import com.carrefour.kata.presentation.dto.SpendPointsResponse;
import com.carrefour.kata.presentation.dto.VoucherResponse;
import com.carrefour.kata.presentation.mapper.AccountMapper;
import com.carrefour.kata.presentation.mapper.VoucherMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour les opérations sur les comptes de fidélité.
 *
 * <p>L'expiration des points et les notifications d'expiration sont intentionnellement absentes ici :
 * ce sont des opérations batch déclenchées par {@link com.carrefour.kata.presentation.scheduler.PointsLifecycleScheduler},
 * et non par des requêtes HTTP individuelles.</p>
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Loyalty Account", description = "Operations on loyalty accounts: earn, spend, vouchers")
@RequiredArgsConstructor
public class AccountController {

    private final GetBalanceUseCase getBalanceUseCase;
    private final EarnPointsUseCase earnPointsUseCase;
    private final SpendPointsUseCase spendPointsUseCase;
    private final IssueVoucherUseCase issueVoucherUseCase;
    private final AccountMapper accountMapper;
    private final VoucherMapper voucherMapper;

    @GetMapping("/{customerId}/balance")
    @Operation(summary = "Get balance", description = "Returns the current points balance and lot details for a loyalty account.")
    @ApiResponse(responseCode = "200", description = "Balance retrieved")
    @ApiResponse(responseCode = "404", description = "Account not found")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String customerId) {
        var response = getBalanceUseCase.execute(new GetBalanceQuery(customerId));
        return ResponseEntity.ok(accountMapper.toBalanceResponse(response));
    }

    @PostMapping("/{customerId}/earn")
    @Operation(summary = "Earn points", description = "Credits points to a loyalty account. Creates the account if it doesn't exist.")
    @ApiResponse(responseCode = "201", description = "Points credited")
    public ResponseEntity<EarnPointsResponse> earn(
            @PathVariable String customerId,
            @Valid @RequestBody EarnPointsRequest request) {
        var command = new EarnPointsCommand(customerId, request.points(), request.expiresAt());
        var response = earnPointsUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountMapper.toEarnPointsResponse(response));
    }

    @PostMapping("/{customerId}/spend")
    @Operation(summary = "Spend points", description = "Debits points from a loyalty account for a payment or donation.")
    @ApiResponse(responseCode = "200", description = "Points spent")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @ApiResponse(responseCode = "422", description = "Insufficient points")
    public ResponseEntity<SpendPointsResponse> spend(
            @PathVariable String customerId,
            @Valid @RequestBody SpendPointsRequest request) {
        SpendingType type;
        try {
            type = SpendingType.valueOf(request.type().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid spending type: " + request.type());
        }
        var command = new SpendPointsCommand(customerId, request.points(), type);
        var response = spendPointsUseCase.execute(command);
        return ResponseEntity.ok(accountMapper.toSpendPointsResponse(response));
    }

    @PostMapping("/{customerId}/vouchers")
    @Operation(summary = "Issue voucher", description = "Converts loyalty points into a voucher.")
    @ApiResponse(responseCode = "201", description = "Voucher issued")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @ApiResponse(responseCode = "422", description = "Insufficient points")
    public ResponseEntity<VoucherResponse> issueVoucher(
            @PathVariable String customerId,
            @Valid @RequestBody IssueVoucherRequest request) {
        var command = new IssueVoucherCommand(customerId, request.points());
        var response = issueVoucherUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(voucherMapper.toVoucherResponse(response));
    }
}
