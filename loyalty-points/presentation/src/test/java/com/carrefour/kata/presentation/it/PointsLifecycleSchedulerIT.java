package com.carrefour.kata.presentation.it;

import com.carrefour.kata.presentation.dto.BalanceResponse;
import com.carrefour.kata.presentation.dto.EarnPointsRequest;
import com.carrefour.kata.presentation.dto.EarnPointsResponse;
import com.carrefour.kata.presentation.scheduler.PointsLifecycleScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link PointsLifecycleScheduler}.
 *
 * <p>The scheduler methods are called directly (not via cron) so the tests
 * are deterministic regardless of execution time. State is set up through
 * the REST API and verified through the balance endpoint.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PointsLifecycleSchedulerIT {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private PointsLifecycleScheduler scheduler;

    private static String newCustomerId() {
        return "sched-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void earn(String customerId, int points, LocalDate expiresAt) {
        rest.postForEntity(
                "/api/accounts/" + customerId + "/earn",
                new EarnPointsRequest(points, expiresAt),
                EarnPointsResponse.class);
    }

    private BalanceResponse balance(String customerId) {
        return rest.getForEntity(
                "/api/accounts/" + customerId + "/balance",
                BalanceResponse.class).getBody();
    }

    // -------------------------------------------------------------------------
    // expirePoints()
    // -------------------------------------------------------------------------

    @Test
    void expirePoints_removes_expired_lots_across_all_accounts() {
        String custExpired = newCustomerId();
        String custValid = newCustomerId();

        // custExpired: one expired lot + one valid lot
        earn(custExpired, 100, LocalDate.now().minusDays(1));  // expired yesterday
        earn(custExpired, 50, LocalDate.now().plusYears(1));   // still valid

        // custValid: only valid lots
        earn(custValid, 200, LocalDate.now().plusYears(1));

        scheduler.expirePoints();

        assertThat(balance(custExpired).balance()).isEqualTo(50);
        assertThat(balance(custExpired).lots()).hasSize(1);

        assertThat(balance(custValid).balance()).isEqualTo(200); // untouched
    }

    @Test
    void expirePoints_is_idempotent_when_no_lots_are_expired() {
        String customerId = newCustomerId();
        earn(customerId, 100, LocalDate.now().plusYears(1));

        scheduler.expirePoints();
        scheduler.expirePoints(); // second run must be a no-op

        assertThat(balance(customerId).balance()).isEqualTo(100);
    }

    // -------------------------------------------------------------------------
    // notifyExpiringPoints()
    // -------------------------------------------------------------------------

    @Test
    void notifyExpiringPoints_does_not_throw_for_mixed_accounts() {
        // One account with expiring-soon lots, one without — the scheduler must
        // complete without exception regardless of notification outcomes
        String custSoon = newCustomerId();
        String custLater = newCustomerId();

        earn(custSoon, 80, LocalDate.now().plusDays(3));    // inside notice window
        earn(custLater, 120, LocalDate.now().plusMonths(6)); // outside notice window

        // NotificationPort is a no-op bean in tests (LoggingNotificationAdapter or similar);
        // the important assertion here is that the scheduler completes without error.
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(scheduler::notifyExpiringPoints);
    }
}
