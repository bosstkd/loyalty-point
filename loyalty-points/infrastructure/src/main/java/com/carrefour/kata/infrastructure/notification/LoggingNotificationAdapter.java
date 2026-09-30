package com.carrefour.kata.infrastructure.notification;

import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.shared.NotificationPort;
import com.carrefour.kata.domain.shared.Points;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class LoggingNotificationAdapter implements NotificationPort {

    @Override
    public void notifyExpiringSoon(CustomerId customerId, Points points, LocalDate expiresAt) {
        log.info("[NOTIFICATION] Customer {} : {} points expiring on {}",
                customerId.value(), points.value(), expiresAt);
    }
}
