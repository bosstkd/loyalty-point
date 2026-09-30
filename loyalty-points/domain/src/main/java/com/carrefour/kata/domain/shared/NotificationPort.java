package com.carrefour.kata.domain.shared;

import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.shared.Points;

import java.time.LocalDate;

public interface NotificationPort {
    void notifyExpiringSoon(CustomerId customerId, Points points, LocalDate expiresAt);
}
