package com.example.order.domain.port;

import java.time.Instant;

/**
 * PORT — Driven port for the system clock.
 *
 * Using a port instead of Instant.now() directly makes time deterministic in tests.
 * Implementations: production uses wall-clock, tests use a fixed clock.
 */
public interface ClockPort {

    /**
     * @return the current instant according to this clock
     */
    Instant now();
}
