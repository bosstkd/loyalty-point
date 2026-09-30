package com.carrefour.kata.presentation.it;

import com.carrefour.kata.presentation.dto.BalanceResponse;
import com.carrefour.kata.presentation.dto.EarnPointsRequest;
import com.carrefour.kata.presentation.dto.EarnPointsResponse;
import com.carrefour.kata.presentation.dto.IssueVoucherRequest;
import com.carrefour.kata.presentation.dto.SpendPointsRequest;
import com.carrefour.kata.presentation.dto.SpendPointsResponse;
import com.carrefour.kata.presentation.dto.VoucherResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests: HTTP → Controller → UseCase → Domain → H2.
 *
 * <p>Each test uses a unique {@code customerId} (UUID-based) so tests are
 * completely isolated without needing transaction rollback.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountIT {

    @Autowired
    private TestRestTemplate rest;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String newCustomerId() {
        return "cust-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private ResponseEntity<EarnPointsResponse> earn(String customerId, int points, LocalDate expiresAt) {
        return rest.postForEntity(
                "/api/accounts/" + customerId + "/earn",
                new EarnPointsRequest(points, expiresAt),
                EarnPointsResponse.class);
    }

    private ResponseEntity<SpendPointsResponse> spend(String customerId, int points) {
        return rest.postForEntity(
                "/api/accounts/" + customerId + "/spend",
                new SpendPointsRequest(points, "PAYMENT"),
                SpendPointsResponse.class);
    }

    private ResponseEntity<BalanceResponse> balance(String customerId) {
        return rest.getForEntity("/api/accounts/" + customerId + "/balance", BalanceResponse.class);
    }

    // -------------------------------------------------------------------------
    // Earn points
    // -------------------------------------------------------------------------

    @Test
    void earn_creates_account_and_returns_201_with_balance() {
        String customerId = newCustomerId();

        var response = earn(customerId, 100, LocalDate.now().plusYears(1));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().earnedPoints()).isEqualTo(100);
        assertThat(response.getBody().newBalance()).isEqualTo(100);
        assertThat(response.getBody().customerId()).isEqualTo(customerId);
    }

    @Test
    void earn_twice_on_same_account_accumulates_balance() {
        String customerId = newCustomerId();

        earn(customerId, 100, LocalDate.now().plusYears(1));
        var second = earn(customerId, 200, LocalDate.now().plusYears(1));

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(second.getBody().newBalance()).isEqualTo(300);
    }

    @Test
    void earn_with_zero_points_returns_400() {
        String customerId = newCustomerId();

        var response = rest.postForEntity(
                "/api/accounts/" + customerId + "/earn",
                new EarnPointsRequest(0, LocalDate.now().plusYears(1)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // -------------------------------------------------------------------------
    // Get balance
    // -------------------------------------------------------------------------

    @Test
    void getBalance_returns_correct_balance_and_lots() {
        String customerId = newCustomerId();
        LocalDate expiresAt = LocalDate.now().plusYears(1);
        earn(customerId, 150, expiresAt);

        var response = balance(customerId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().customerId()).isEqualTo(customerId);
        assertThat(response.getBody().balance()).isEqualTo(150);
        assertThat(response.getBody().lots()).hasSize(1);
        assertThat(response.getBody().lots().get(0).remaining()).isEqualTo(150);
    }

    // -------------------------------------------------------------------------
    // 404 — unknown customer across all mutating/read endpoints
    // -------------------------------------------------------------------------

    static Stream<Arguments> unknownCustomerEndpoints() {
        return Stream.of(
                Arguments.of("GET  /balance",
                        (Function<TestRestTemplate, ResponseEntity<String>>) r ->
                                r.getForEntity("/api/accounts/unknown-customer/balance", String.class)),
                Arguments.of("POST /spend",
                        (Function<TestRestTemplate, ResponseEntity<String>>) r ->
                                r.postForEntity("/api/accounts/unknown-customer/spend",
                                        new SpendPointsRequest(10, "PAYMENT"), String.class)),
                Arguments.of("POST /vouchers",
                        (Function<TestRestTemplate, ResponseEntity<String>>) r ->
                                r.postForEntity("/api/accounts/unknown-customer/vouchers",
                                        new IssueVoucherRequest(100), String.class))
        );
    }

    @ParameterizedTest(name = "{0} returns 404 for unknown customer")
    @MethodSource("unknownCustomerEndpoints")
    void endpoint_returns_404_for_unknown_customer(
            String description,
            Function<TestRestTemplate, ResponseEntity<String>> request) {

        assertThat(request.apply(rest).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // Spend points
    // -------------------------------------------------------------------------

    @Test
    void spend_reduces_balance_and_returns_200() {
        String customerId = newCustomerId();
        earn(customerId, 100, LocalDate.now().plusYears(1));

        var response = spend(customerId, 30);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().spentPoints()).isEqualTo(30);
        assertThat(response.getBody().remainingBalance()).isEqualTo(70);
    }

    @Test
    void spend_returns_422_when_balance_is_insufficient() {
        String customerId = newCustomerId();
        earn(customerId, 50, LocalDate.now().plusYears(1));

        var response = rest.postForEntity(
                "/api/accounts/" + customerId + "/spend",
                new SpendPointsRequest(200, "PAYMENT"),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // -------------------------------------------------------------------------
    // FIFO invariant
    // -------------------------------------------------------------------------

    @Test
    void spend_consumes_oldest_lot_first() {
        String customerId = newCustomerId();
        earn(customerId, 100, LocalDate.now().plusMonths(6));  // older lot, expires sooner
        earn(customerId, 200, LocalDate.now().plusYears(2));   // newer lot, expires later

        spend(customerId, 100);

        var balanceResponse = balance(customerId);
        assertThat(balanceResponse.getBody().balance()).isEqualTo(200);
        assertThat(balanceResponse.getBody().lots()).hasSize(1);
        assertThat(balanceResponse.getBody().lots().get(0).remaining()).isEqualTo(200);
    }

    @Test
    void spend_spans_multiple_lots_fifo() {
        String customerId = newCustomerId();
        earn(customerId, 80, LocalDate.now().plusMonths(6));
        earn(customerId, 100, LocalDate.now().plusYears(2));

        spend(customerId, 100); // consumes 80 from first lot + 20 from second

        var balanceResponse = balance(customerId);
        assertThat(balanceResponse.getBody().balance()).isEqualTo(80);
        assertThat(balanceResponse.getBody().lots()).hasSize(1);
        assertThat(balanceResponse.getBody().lots().get(0).remaining()).isEqualTo(80);
    }

    // -------------------------------------------------------------------------
    // Issue voucher
    // -------------------------------------------------------------------------

    @Test
    void issueVoucher_deducts_points_and_returns_201() {
        String customerId = newCustomerId();
        earn(customerId, 200, LocalDate.now().plusYears(1));

        var response = rest.postForEntity(
                "/api/accounts/" + customerId + "/vouchers",
                new IssueVoucherRequest(100),
                VoucherResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().value()).isEqualTo(100);
        assertThat(response.getBody().customerId()).isEqualTo(customerId);
        assertThat(response.getBody().voucherId()).isNotBlank();
        assertThat(response.getBody().issuedAt()).isNotNull();
        assertThat(response.getBody().expiresAt()).isAfter(LocalDate.now());

        assertThat(balance(customerId).getBody().balance()).isEqualTo(100);
    }

    @Test
    void issueVoucher_returns_422_when_insufficient_points() {
        String customerId = newCustomerId();
        earn(customerId, 50, LocalDate.now().plusYears(1));

        var response = rest.postForEntity(
                "/api/accounts/" + customerId + "/vouchers",
                new IssueVoucherRequest(200),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
