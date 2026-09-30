package com.carrefour.kata.infrastructure.clock;

import com.carrefour.kata.domain.shared.ClockPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SystemClockAdapter implements ClockPort {

    @Override
    public LocalDate today() {
        return LocalDate.now();
    }
}
