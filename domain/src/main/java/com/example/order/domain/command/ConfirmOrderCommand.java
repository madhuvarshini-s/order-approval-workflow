package com.example.order.domain.command;

/**
 * Command: intent to confirm a draft Order, transitioning it to CONFIRMED.
 */
public record ConfirmOrderCommand(String orderId) {
    public ConfirmOrderCommand {
        if (orderId == null || orderId.isBlank()) throw new IllegalArgumentException("orderId required");
    }
}
