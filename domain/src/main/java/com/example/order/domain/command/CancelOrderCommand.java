package com.example.order.domain.command;

/**
 * Command: intent to cancel an Order, providing a business reason.
 */
public record CancelOrderCommand(String orderId, String reason) {
    public CancelOrderCommand {
        if (orderId == null || orderId.isBlank()) throw new IllegalArgumentException("orderId required");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("reason required");
    }
}
