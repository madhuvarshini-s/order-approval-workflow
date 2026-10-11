package com.example.order.domain.command;

/**
 * Command: intent to record a payment against a CONFIRMED Order.
 */
public record RecordPaymentCommand(String orderId) {
    public RecordPaymentCommand {
        if (orderId == null || orderId.isBlank()) throw new IllegalArgumentException("orderId required");
    }
}
