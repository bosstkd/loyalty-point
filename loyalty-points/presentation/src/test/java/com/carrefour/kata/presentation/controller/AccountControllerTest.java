package com.carrefour.kata.presentation.controller;

import com.carrefour.kata.application.exception.AccountNotFoundException;
import com.carrefour.kata.application.usecase.earnpoints.EarnPointsResponse;
import com.carrefour.kata.application.usecase.earnpoints.EarnPointsUseCase;
import com.carrefour.kata.application.usecase.expirepoints.ExpirePointsUseCase;
import com.carrefour.kata.application.usecase.getbalance.GetBalanceResponse;
import com.carrefour.kata.application.usecase.getbalance.GetBalanceUseCase;
import com.carrefour.kata.application.usecase.issuevoucher.IssueVoucherUseCase;
import com.carrefour.kata.application.usecase.notifyexpiring.NotifyExpiringPointsUseCase;
import com.carrefour.kata.application.usecase.spendpoints.SpendPointsResponse;
import com.carrefour.kata.application.usecase.spendpoints.SpendPointsUseCase;
import com.carrefour.kata.domain.account.ExpirationNotificationService;
import com.carrefour.kata.domain.account.exception.InsufficientPointsException;
import com.carrefour.kata.domain.account.repository.LoyaltyAccountRepository;
import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.shared.ClockPort;
import com.carrefour.kata.domain.shared.NotificationPort;
import com.carrefour.kata.domain.shared.Points;
import com.carrefour.kata.domain.voucher.VoucherIssuanceService;
import com.carrefour.kata.domain.voucher.repository.VoucherRepository;
import com.carrefour.kata.presentation.dto.BalanceResponse;
import com.carrefour.kata.presentation.dto.EarnPointsRequest;
import com.carrefour.kata.presentation.dto.SpendPointsRequest;
import com.carrefour.kata.presentation.error.GlobalExceptionHandler;
import com.carrefour.kata.presentation.mapper.AccountMapper;
import com.carrefour.kata.presentation.mapper.VoucherMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetBalanceUseCase getBalanceUseCase;

    @MockBean
    private EarnPointsUseCase earnPointsUseCase;

    @MockBean
    private SpendPointsUseCase spendPointsUseCase;

    @MockBean
    private IssueVoucherUseCase issueVoucherUseCase;

    @MockBean
    private AccountMapper accountMapper;

    @MockBean
    private VoucherMapper voucherMapper;

    // Infrastructure beans required by UseCaseConfiguration (loaded by @WebMvcTest).
    // ExpirePointsUseCase and NotifyExpiringPointsUseCase are no longer injected by
    // AccountController (they moved to the scheduler), but UseCaseConfiguration still
    // declares them — the mocks below satisfy their constructor dependencies cleanly.
    @MockBean
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @MockBean
    private VoucherRepository voucherRepository;

    @MockBean
    private ClockPort clock;

    @MockBean
    private NotificationPort notificationPort;

    @MockBean
    private VoucherIssuanceService voucherIssuanceService;

    @MockBean
    private ExpirationNotificationService expirationNotificationService;

    @MockBean
    private ExpirePointsUseCase expirePointsUseCase;

    @MockBean
    private NotifyExpiringPointsUseCase notifyExpiringPointsUseCase;

    // --- GET balance ---

    @Test
    void getBalance_returns200_whenAccountExists() throws Exception {
        var useCaseResponse = new GetBalanceResponse("cust-1", 100, List.of());
        var dto = new BalanceResponse("cust-1", 100, List.of());

        when(getBalanceUseCase.execute(any())).thenReturn(useCaseResponse);
        when(accountMapper.toBalanceResponse(useCaseResponse)).thenReturn(dto);

        mockMvc.perform(get("/api/accounts/cust-1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("cust-1"))
                .andExpect(jsonPath("$.balance").value(100));
    }

    @Test
    void getBalance_returns404_whenAccountNotFound() throws Exception {
        when(getBalanceUseCase.execute(any()))
                .thenThrow(new AccountNotFoundException("cust-99"));

        mockMvc.perform(get("/api/accounts/cust-99/balance"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    // --- POST earn ---

    @Test
    void earn_returns201_withValidRequest() throws Exception {
        var expiresAt = LocalDate.now().plusYears(1);
        var request = new EarnPointsRequest(50, expiresAt);
        var useCaseResponse = new EarnPointsResponse("cust-1", 50, 150, expiresAt);
        var dto = new com.carrefour.kata.presentation.dto.EarnPointsResponse("cust-1", 50, 150, expiresAt);

        when(earnPointsUseCase.execute(any())).thenReturn(useCaseResponse);
        when(accountMapper.toEarnPointsResponse(useCaseResponse)).thenReturn(dto);

        mockMvc.perform(post("/api/accounts/cust-1/earn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.earnedPoints").value(50));
    }

    @ParameterizedTest(name = "earn with points={0} returns 400")
    @ValueSource(ints = {0, -1, -100})
    void earn_returns400_for_invalid_points(int invalidPoints) throws Exception {
        var request = new EarnPointsRequest(invalidPoints, LocalDate.now().plusYears(1));

        mockMvc.perform(post("/api/accounts/cust-1/earn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- POST spend ---

    @Test
    void spend_returns200_withValidRequest() throws Exception {
        var request = new SpendPointsRequest(30, "PAYMENT");
        var useCaseResponse = new SpendPointsResponse("cust-1", 30, 70);
        var dto = new com.carrefour.kata.presentation.dto.SpendPointsResponse("cust-1", 30, 70);

        when(spendPointsUseCase.execute(any())).thenReturn(useCaseResponse);
        when(accountMapper.toSpendPointsResponse(useCaseResponse)).thenReturn(dto);

        mockMvc.perform(post("/api/accounts/cust-1/spend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spentPoints").value(30));
    }

    @Test
    void spend_returns422_whenInsufficientPoints() throws Exception {
        var request = new SpendPointsRequest(999, "PAYMENT");

        when(spendPointsUseCase.execute(any()))
                .thenThrow(new InsufficientPointsException(
                        CustomerId.of("cust-1"), Points.of(999), Points.of(10)));

        mockMvc.perform(post("/api/accounts/cust-1/spend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }
}
