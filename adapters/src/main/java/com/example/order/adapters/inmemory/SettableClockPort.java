package com.example.order.adapters.inmemory;

import com.example.order.domain.port.ClockPort;

import java.time.Duration;
import java.time.Instant;

/**
 * In-Memory / Deterministic adapter for ClockPort.
 *
 * Allows tests to freeze or advance time deterministically.
 */
public class SettableClockPort implements ClockPort {

    private Instant currentInstant;

    public SettableClockPort(Instant initialInstant) {
        this.currentInstant = initialInstant;
    }

    public static SettableClockPort fixed(Instant instant) {
        return new SettableClockPort(instant);
    }

    public static SettableClockPort system() {
        return new SettableClockPort(Instant.now());
    }

    @Override
    public Instant now() {
        return currentInstant;
    }

    public void set(Instant newInstant) {
        this.currentInstant = newInstant;
    }

    public void advance(Duration duration) {
        this.currentInstant = this.currentInstant.plus(duration);
    }
}
